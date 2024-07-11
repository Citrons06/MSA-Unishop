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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final RedissonClient redissonClient;
    private final ProductProducer productProducer;
    private final PayRepository payRepository;
    private static final String ORDER_QUEUE_KEY = "order:queue";
    private static final String STOCK_KEY = "stock:";

    public void enqueueOrder(PayRequest payRequest) {
        RQueue<PayRequest> orderQueue = redissonClient.getQueue(ORDER_QUEUE_KEY);
        orderQueue.add(payRequest);
    }

    @Scheduled(fixedRate = 100) // 100ms마다 실행
    public void processOrders() {
        RQueue<PayRequest> orderQueue = redissonClient.getQueue(ORDER_QUEUE_KEY);
        PayRequest payRequest = orderQueue.poll();
        if (payRequest != null) {
            String stockKey = STOCK_KEY + payRequest.getItemId();
            RAtomicLong stock = redissonClient.getAtomicLong(stockKey);

            if (stock.decrementAndGet() >= 0) {
                processOrder(payRequest);
            } else {
                stock.incrementAndGet(); // 재고를 원래대로 되돌림
                failOrder(payRequest);
            }
        }
    }

    private void processOrder(PayRequest payRequest) {
        // 주문 처리 로직
        PayEvent stockDeductEvent = createStockDeductEvent(payRequest);
        productProducer.sendProductEvent(stockDeductEvent);

        // Pay 엔티티 상태 업데이트
        Pay pay = payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(payRequest.getUsername(), PayStatus.PAY_START)
                .orElseThrow(() -> new CommonException(ErrorCode.PAY_NOT_FOUND));
        pay.setPayStatus(PayStatus.STOCK_DEDUCTED);
        payRepository.save(pay);

        log.info("주문 처리 완료: 사용자 {}, 상품 ID {}", payRequest.getUsername(), payRequest.getItemId());
    }

    private void failOrder(PayRequest payRequest) {
        // 주문 실패 처리 로직
        log.warn("재고 부족으로 주문 실패: 사용자 {}, 상품 ID {}", payRequest.getUsername(), payRequest.getItemId());
    }

    private PayEvent createStockDeductEvent(PayRequest payRequest) {
        return new PayEvent("STOCK_DEDUCT", payRequest.getUsername(),
                payRequest.getItemId(), payRequest.getQuantity(), payRequest.getAmount(), 0);
    }
}