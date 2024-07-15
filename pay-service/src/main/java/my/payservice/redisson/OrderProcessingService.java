package my.payservice.redisson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.exception.CommonException;
import my.payservice.exception.ErrorCode;
import my.payservice.kafka.ProductProducer;
import my.payservice.kafka.event.PayEvent;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import my.payservice.pay.repository.PayRepository;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final RedissonClient redissonClient;
    private final ProductProducer productProducer;
    private final PayRepository payRepository;
    private static final String ORDER_QUEUE_KEY = "order:queue";
    private static final String STOCK_KEY = "stock:";
    private static final int BATCH_SIZE = 100;

    public void enqueueOrder(PayRequest payRequest) {
        RQueue<PayRequest> orderQueue = redissonClient.getQueue(ORDER_QUEUE_KEY);
        orderQueue.add(payRequest);
    }

    @Scheduled(fixedRate = 10)
    public void processOrders() {
        RQueue<PayRequest> orderQueue = redissonClient.getQueue(ORDER_QUEUE_KEY);
        List<PayRequest> batch = new ArrayList<>();

        for (int i = 0; i < BATCH_SIZE; i++) {
            PayRequest payRequest = orderQueue.poll();
            if (payRequest == null) break;
            batch.add(payRequest);
        }

        processBatch(batch);
    }

    private void processBatch(List<PayRequest> batch) {
        for (PayRequest payRequest : batch) {
            processOrder(payRequest);
        }
    }

    @Transactional
    @CachePut(value = "PayStatus", key = "#payRequest.username")
    public void processOrder(PayRequest payRequest) {
        Pay pay = payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(payRequest.getUsername(), PayStatus.STOCK_DEDUCTED)
                .orElseThrow(() -> new CommonException(ErrorCode.PAY_NOT_FOUND));

        // Redis 에서 재고가 이미 감소되었으므로 여기서는 재고 감소 결과를 확인하고 기록
        boolean stockDeductionSuccess = checkAndRecordStockDeduction(payRequest);

        if (stockDeductionSuccess) {
            // 재고 감소 성공 이벤트 발송 (다른 서비스와 동기화)
            PayEvent stockDeductedEvent = createStockDeductedEvent(payRequest);
            productProducer.sendProductEvent(stockDeductedEvent);
        } else {
            // 재고 부족 상황 처리
            handleInsufficientStock(pay, payRequest);
        }

        if (Math.random() <= 0.2) {
            handleCancelledOrder(payRequest);
            pay.setPayStatus(PayStatus.PAY_CANCEL);
            payRepository.save(pay);
            return;
        }

        log.info("주문 처리 완료: 사용자 {}, 상품 ID {}", payRequest.getUsername(), payRequest.getItemId());
    }

    @Cacheable(value = "payStatus", key = "#username")
    @Transactional(readOnly = true)
    public PayStatus getPayStatus(String username) {
        Optional<Pay> payOpt = payRepository.findFirstByUsernameOrderByCreatedDateDesc(username);
        return payOpt.map(Pay::getPayStatus).orElse(null);
    }

    private void handleInsufficientStock(Pay pay, PayRequest payRequest) {
        updatePayStatus(pay, PayStatus.PAY_FAILED);

        log.warn("재고 부족으로 주문 실패: 사용자 {}, 상품 ID {}", payRequest.getUsername(), payRequest.getItemId());
    }

    private boolean checkAndRecordStockDeduction(PayRequest payRequest) {
        String stockKey = STOCK_KEY + payRequest.getItemId();
        RAtomicLong stock = redissonClient.getAtomicLong(stockKey);
        return stock.get() >= 0;
    }

    private void handleCancelledOrder(PayRequest payRequest) {
        log.info("취소된 주문 처리: 사용자 {}, 상품 ID {}", payRequest.getUsername(), payRequest.getItemId());
        PayEvent stockRecoverEvent = createStockRecoverEvent(payRequest);
        productProducer.sendProductEvent(stockRecoverEvent);
    }

    private void updatePayStatus(Pay pay, PayStatus status) {
        pay.setPayStatus(status);
        payRepository.save(pay);
    }

    private PayEvent createStockDeductedEvent(PayRequest payRequest) {
        return new PayEvent("STOCK_DEDUCTED", payRequest.getUsername(),
                payRequest.getItemId(), payRequest.getQuantity(), payRequest.getAmount(), 0);
    }

    private PayEvent createStockRecoverEvent(PayRequest payRequest) {
        return new PayEvent("STOCK_RECOVER", payRequest.getUsername(),
                payRequest.getItemId(), payRequest.getQuantity(), payRequest.getAmount(), 0);
    }
}