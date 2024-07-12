package my.payservice.pay.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.aop.DistributedLock;
import my.payservice.exception.CommonException;
import my.payservice.exception.ErrorCode;
import my.payservice.kafka.ProcessProducer;
import my.payservice.kafka.ProductProducer;
import my.payservice.kafka.event.ProcessEvent;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.dto.ProcessRequest;
import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import my.payservice.kafka.event.PayEvent;
import my.payservice.pay.repository.PayRepository;
import my.payservice.redisson.OrderProcessingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @DistributedLock(key = "'stock:' + #payRequest.itemId", waitTime = 5, leaseTime = 10)
    public ResponseEntity<?> initiatePayment(PayRequest payRequest) {
        try {
            // 결제 정보 임시 저장
            Pay pay = createInitialPay(payRequest, PayStatus.STOCK_CHECKING);
            payRepository.save(pay);

            // 주문 큐에 추가
            orderProcessingService.enqueueOrder(payRequest);

            log.info("결제 진입 요청 및 재고 확인 요청: 사용자 {}, 상품 ID {}", payRequest.getUsername(), payRequest.getItemId());
            return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제 진입 요청이 접수되었습니다. 재고 확인 중입니다.\"}");
        } catch (Exception e) {
            log.error("결제 진입 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제 진입에 실패하였습니다.\"}");
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void processPayment(ProcessRequest processRequest, String username) {
        Retry retry = retryRegistry.retry("processPayment");
        retry.executeRunnable(() -> {
            try {
                Pay pay = findPayByUsernameAndStatus(username, PayStatus.STOCK_DEDUCTED);

                if (Math.random() <= 0.2) {
                    handlePaymentFailure(processRequest, username);
                } else {
                    handlePaymentSuccess(pay, processRequest);
                }
            } catch (CommonException e) {
                handleStockShortage(e, processRequest, username);
            } catch (Exception e) {
                handleGeneralError(e, processRequest, username);
            }
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updatePaymentStatus(PayEvent payEvent) {
        Retry retry = retryRegistry.retry("updatePaymentStatus");
        retry.executeRunnable(() -> {
            Pay pay = findPayByUsernameAndStatus(payEvent.getUsername(), PayStatus.STOCK_DEDUCTED);
            PayStatus newStatus = determineNewPaymentStatus(payEvent, pay);

            if (newStatus != pay.getPayStatus()) {
                updatePayStatus(pay, newStatus, payEvent);
            } else {
                log.info("결제 상태 변경 없음: 사용자: {}, 상품ID: {}, 상태: {}", pay.getUsername(), payEvent.getItemId(), newStatus);
            }
        });
    }

    private Pay createInitialPay(PayRequest payRequest, PayStatus status) {
        return Pay.builder()
                .amount(payRequest.getAmount())
                .payStatus(status)
                .username(payRequest.getUsername())
                .build();
    }

    private void handlePaymentFailure(ProcessRequest processRequest, String username) {
        updatePaymentStatus(new PayEvent("PAY_FAILED", username,
                processRequest.getItemId(), processRequest.getQuantity(),
                processRequest.getAmount(), 0));
        log.info("고객 귀책으로 결제 실패: {} {}", username, processRequest.getItemId());
    }

    private void handleStockShortage(CommonException e, ProcessRequest processRequest, String username) {
        log.error("재고 부족: {}", e.getMessage());
        updatePaymentStatus(new PayEvent("PAY_FAILED", username, processRequest.getItemId(),
                processRequest.getQuantity(), processRequest.getAmount(), 0));
        throw new CommonException(ErrorCode.NOT_ENOUGH_STOCK);
    }

    private void handleGeneralError(Exception e, ProcessRequest processRequest, String username) {
        log.error("결제 처리 중 오류 발생: {}", e.getMessage());
        updatePaymentStatus(new PayEvent("PAY_FAILED", username, processRequest.getItemId(),
                processRequest.getQuantity(), processRequest.getAmount(), 0));
        productProducer.sendProductEvent(new PayEvent("STOCK_RECOVER", username,
                processRequest.getItemId(), processRequest.getQuantity(), processRequest.getAmount(), 0));
        throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private PayStatus determineNewPaymentStatus(PayEvent payEvent, Pay pay) {
        return switch (payEvent.getStatus()) {
            case "PAY_FAILED" -> PayStatus.PAY_FAILED;
            case "PAY_COMPLETE" -> PayStatus.PAY_COMPLETE;
            default -> {
                log.warn("잘못된 결제 상태: {}", payEvent.getStatus());
                yield pay.getPayStatus(); // 현재 상태를 유지
            }
        };
    }

    private void updatePayStatus(Pay pay, PayStatus newStatus, PayEvent payEvent) {
        pay.setPayStatus(newStatus);
        payRepository.save(pay);
        log.info("결제 상태 업데이트: 사용자: {}, 상품ID: {}, 새 상태: {}", pay.getUsername(), payEvent.getItemId(), newStatus);

        if (newStatus == PayStatus.PAY_FAILED) {
            handlePaymentCancelOrFailure(payEvent);
        }
    }

    private void handlePaymentCancelOrFailure(PayEvent payEvent) {
        PayEvent recoverEvent = new PayEvent("STOCK_RECOVER", payEvent.getUsername(),
                payEvent.getItemId(), payEvent.getQuantity(),
                payEvent.getAmount(), payEvent.getItemQuantity());
        productProducer.sendProductEvent(recoverEvent);
        log.info("결제 취소로 인한 재고 회복 요청: 사용자: {}, 상품ID: {}", payEvent.getUsername(), payEvent.getItemId());
    }

    private void handlePaymentSuccess(Pay pay, ProcessRequest processRequest) {
        ProcessEvent processEvent = new ProcessEvent("PAY_COMPLETE", pay.getUsername(), processRequest.getOrderUsername(),
                processRequest.getItemId(), processRequest.getQuantity(),
                processRequest.getAmount(),
                processRequest.getOrderAddress(), processRequest.getOrderTel());
        payProducer.sendProcessEvent(processEvent);

        updatePaymentStatus(new PayEvent("PAY_COMPLETE", pay.getUsername(),
                processRequest.getItemId(), processRequest.getQuantity(),
                processRequest.getAmount(), 0));

        log.info("결제 성공: 사용자: {}, 상품ID: {}", pay.getUsername(), processRequest.getItemId());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void checkPayStatus(String username) {
        Optional<Pay> findPayOpt = payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(username, PayStatus.STOCK_DEDUCTED);
        if (findPayOpt.isEmpty()) {
            log.warn("잘못된 결제 상태: 사용자: {}", username);
            throw new CommonException(ErrorCode.INVALID_PAY_STATUS);
        }
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
}