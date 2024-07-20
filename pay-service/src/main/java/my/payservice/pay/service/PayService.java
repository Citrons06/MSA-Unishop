package my.payservice.pay.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.adapter.ProductAdapter;
import my.payservice.adapter.SoldTimeDto;
import my.payservice.exception.CommonException;
import my.payservice.exception.ErrorCode;
import my.payservice.kafka.ProcessProducer;
import my.payservice.kafka.ProductProducer;
import my.payservice.kafka.event.ProcessEvent;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.dto.PayResponse;
import my.payservice.pay.dto.ProcessRequest;
import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import my.payservice.kafka.event.PayEvent;
import my.payservice.pay.repository.PayRepository;
import my.payservice.redisson.OrderProcessingService;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PayService {

    private final ProcessProducer payProducer;
    private final PayRepository payRepository;
    private final ProductProducer productProducer;
    private final RetryRegistry retryRegistry;
    private final OrderProcessingService orderProcessingService;
    private final RedissonClient redissonClient;
    private final ProductAdapter productAdapter;

    @Transactional
    //@DistributedLock(key = "'stock:' + #payRequest.itemId", waitTime = 5, leaseTime = 10)
    public ResponseEntity<?> initiatePayment(PayRequest payRequest) {
        try {
            SoldTimeDto soldTime = getSoldTime(payRequest.getItemId());

            if (soldTime.isPreOrder() && LocalDateTime.now().isBefore(soldTime.getPreOrderStartAt())) {
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\" : \"예약 판매 시작 전입니다.\"}");
            }

            String stockKey = "stock:" + payRequest.getItemId();
            RAtomicLong stock = redissonClient.getAtomicLong(stockKey);

            if (stock.get() < payRequest.getQuantity()) {
                Pay pay = createPay(payRequest, PayStatus.PAY_FAILED);
                payRepository.save(pay);
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\" : \"재고가 부족합니다.\"}");
            }

            if (stock.addAndGet(-payRequest.getQuantity()) < 0) {
                stock.addAndGet(payRequest.getQuantity()); // 롤백
                Pay pay = createPay(payRequest, PayStatus.PAY_FAILED);
                payRepository.save(pay);
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\" : \"재고가 부족합니다.\"}");
            }

            Pay pay = createPay(payRequest, PayStatus.STOCK_DEDUCTED);
            payRepository.save(pay);

            CompletableFuture.runAsync(() -> {
                orderProcessingService.enqueueOrder(payRequest);
            });

            log.info("결제 진입 요청 및 재고 확인 요청: 사용자 {}, 상품 ID {}", payRequest.getUsername(), payRequest.getItemId());
            return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제 진입 요청 성공\"}");
        } catch (Exception e) {
            log.error("결제 진입 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제 진입에 실패하였습니다.\"}");
        }
    }

    //@DistributedLock(key = "'payment:' + #processRequest.itemId", waitTime = 5, leaseTime = 10)
    @Transactional
    @CachePut(value = "payStatus", key = "#username")
    public PayStatus processPayment(ProcessRequest processRequest, String username) {
        Retry retry = retryRegistry.retry("processPayment");
        return retry.executeSupplier(() -> {
            try {
                Pay pay = findPayByUsernameAndStatus(username, PayStatus.STOCK_DEDUCTED);
                if (pay == null) {
                    throw new CommonException(ErrorCode.INVALID_PAY_STATUS);
                }

                if (Math.random() <= 0.2) {
                    return handlePaymentFailure(processRequest, username);
                } else {
                    PayStatus payStatus = updatePaymentStatus(pay, processRequest);
                    createOrder(processRequest, username);
                    return payStatus;
                }
            } catch (CommonException e) {
                if (e.getErrorCode() == ErrorCode.NOT_ENOUGH_STOCK) {
                    return handleStockShortage(e, processRequest, username);
                } else {
                    throw e;
                }
            } catch (Exception e) {
                return handleGeneralError(e, processRequest, username);
            }
        });
    }

    @Cacheable(value = "productInfo", key = "#itemId")
    public SoldTimeDto getSoldTime(Long itemId) {
        return productAdapter.getSoldTime(itemId);
    }

    @Cacheable(value = "payStatus", key = "#username")
    @Transactional(readOnly = true)
    public PayStatus getPayStatus(String username) {
        Optional<Pay> payOpt = payRepository.findFirstByUsernameOrderByCreatedDateDesc(username);
        return payOpt.map(Pay::getPayStatus).orElse(null);
    }

    @CachePut(value = "payStatus", key = "#payEvent.username")
    @Transactional
    public PayStatus updatePaymentStatus(PayEvent payEvent) {
        Retry retry = retryRegistry.retry("updatePaymentStatus");
        return retry.executeSupplier(() -> {
            PayStatus newStatus = determineNewPaymentStatus(payEvent);

            if (newStatus != null) {
                int updatedRows = payRepository.updatePayStatus(payEvent.getUsername(), PayStatus.STOCK_DEDUCTED, newStatus);

                if (updatedRows > 0) {
                    log.info("결제 상태 업데이트: 사용자: {}, 상품ID: {}, 새 상태: {}", payEvent.getUsername(), payEvent.getItemId(), newStatus);
                    if (newStatus == PayStatus.PAY_FAILED) {
                        handlePaymentCancelOrFailure(payEvent);
                    }
                    return newStatus;
                } else {
                    log.warn("결제 상태 업데이트 실패: 사용자: {}, 상품ID: {}", payEvent.getUsername(), payEvent.getItemId());
                }
            }
            return null;
        });
    }

    @CacheEvict(value = "payStatus", key = "#username")
    @Transactional
    public void checkPayStatus(String username) {
        Optional<Pay> findPayOpt = payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(username, PayStatus.STOCK_DEDUCTED);
        if (findPayOpt.isEmpty()) {
            log.warn("잘못된 결제 상태: 사용자: {}", username);
            throw new CommonException(ErrorCode.INVALID_PAY_STATUS);
        }
    }

    private PayStatus determineNewPaymentStatus(PayEvent payEvent) {
        return switch (payEvent.getStatus()) {
            case "PAY_FAILED" -> PayStatus.PAY_FAILED;
            case "PAY_COMPLETE" -> PayStatus.PAY_COMPLETE;
            default -> null;
        };
    }

    private Pay createPay(PayRequest payRequest, PayStatus status) {
        return Pay.builder()
                .amount(payRequest.getAmount())
                .payStatus(status)
                .username(payRequest.getUsername())
                .build();
    }

    private PayStatus handlePaymentFailure(ProcessRequest processRequest, String username) {
        updatePaymentStatus(new PayEvent("PAY_FAILED", username,
                processRequest.getItemId(), processRequest.getQuantity(),
                processRequest.getAmount(), 0));
        log.info("고객 귀책으로 결제 실패: {} {}", username, processRequest.getItemId());

        return PayStatus.PAY_FAILED;
    }

    private PayStatus handleStockShortage(CommonException e, ProcessRequest processRequest, String username) {
        log.error("재고 부족: {}", e.getMessage());
        updatePaymentStatus(new PayEvent("PAY_FAILED", username, processRequest.getItemId(),
                processRequest.getQuantity(), processRequest.getAmount(), 0));

        return PayStatus.PAY_FAILED;
    }

    private PayStatus handleGeneralError(Exception e, ProcessRequest processRequest, String username) {
        log.error("결제 처리 중 오류 발생: {}", e.getMessage());
        updatePaymentStatus(new PayEvent("PAY_FAILED", username, processRequest.getItemId(),
                processRequest.getQuantity(), processRequest.getAmount(), 0));
        productProducer.sendProductEvent(new PayEvent("STOCK_RECOVER", username,
                processRequest.getItemId(), processRequest.getQuantity(), processRequest.getAmount(), 0));

        return PayStatus.PAY_FAILED;
    }

    private void handlePaymentCancelOrFailure(PayEvent payEvent) {
        PayEvent recoverEvent = new PayEvent("STOCK_RECOVER", payEvent.getUsername(),
                payEvent.getItemId(), payEvent.getQuantity(),
                payEvent.getAmount(), payEvent.getItemQuantity());
        productProducer.sendProductEvent(recoverEvent);
        log.info("결제 취소로 인한 재고 회복 요청: 사용자: {}, 상품ID: {}", payEvent.getUsername(), payEvent.getItemId());
    }

    @Transactional
    public PayStatus updatePaymentStatus(Pay pay, ProcessRequest processRequest) {
        updatePaymentStatus(new PayEvent("PAY_COMPLETE", pay.getUsername(),
                processRequest.getItemId(), processRequest.getQuantity(),
                processRequest.getAmount(), 0));

        log.info("결제 성공: 사용자: {}, 상품ID: {}", pay.getUsername(), processRequest.getItemId());

        return PayStatus.PAY_COMPLETE;
    }

    public void createOrder(ProcessRequest processRequest, String username) {
        ProcessEvent processEvent = new ProcessEvent("ORDER_CREATE", username, processRequest.getOrderUsername(),
                processRequest.getItemId(), processRequest.getQuantity(),
                processRequest.getAmount(),
                processRequest.getOrderAddress(), processRequest.getOrderTel());
        payProducer.sendProcessEvent(processEvent);
    }

    private Pay findPayByUsernameAndStatus(String username, PayStatus... statuses) {
        for (PayStatus status : statuses) {
            Optional<Pay> payOpt = payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(username, status);
            if (payOpt.isPresent()) {
                return payOpt.get();
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public PayResponse getLatestPayment(String username) {
        Pay pay = payRepository.findFirstByUsernameOrderByCreatedDateDesc(username)
                .orElseThrow(() -> new CommonException(ErrorCode.PAY_NOT_FOUND));

        return new PayResponse(pay);
    }
}