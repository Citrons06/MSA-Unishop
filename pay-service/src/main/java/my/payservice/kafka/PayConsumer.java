package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.PayEvent;
import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import my.payservice.pay.repository.PayRepository;
import my.payservice.pay.service.PayService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PayConsumer {

    private static final String PAY_TOPIC = "pay-topic";
    private static final String PAY_GROUP_ID = "pay-group";

    private final PayService payService;
    private final PayRepository payRepository;
    private final ProductProducer productProducer;
    private final ConcurrentHashMap<String, AtomicLong> lastProcessedSequence = new ConcurrentHashMap<>();
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @KafkaListener(topics = PAY_TOPIC, groupId = PAY_GROUP_ID, containerFactory = "payEventKafkaListenerContainerFactory")
    public void consume(PayEvent payEvent) {
        log.info("Consumed event: {}", payEvent);

        if (processedEventIds.contains(payEvent.getEventId())) {
            log.warn("Skipping already processed event: {}", payEvent.getEventId());
            return;
        }

        if (!isValidSequence(payEvent)) {
            log.warn("Invalid sequence for event: {}", payEvent);
            return;
        }

        try {
            processEvent(payEvent);
            processedEventIds.add(payEvent.getEventId());
            updateLastProcessedSequence(payEvent);
        } catch (Exception e) {
            log.error("Failed to process event: {}", payEvent, e);
            handleProcessingError(payEvent);
        }
    }

    private boolean isValidSequence(PayEvent payEvent) {
        String key = generateKey(payEvent);
        long currentSequence = payEvent.getSequenceNumber();

        AtomicLong lastSequence = lastProcessedSequence.computeIfAbsent(key, k -> new AtomicLong(0));

        return currentSequence > lastSequence.get();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void processEvent(PayEvent payEvent) {
        switch (payEvent.getStatus()) {
            case "PAY_CANCEL", "PAY_FAILED", "PAY_COMPLETE" ->
                    payService.updatePaymentStatus(payEvent);
            case "STOCK_RECOVER_SUCCESS" ->
                    log.info("재고 복구를 성공했습니다.");
            case "STOCK_RECOVER_FAILED" ->
                    log.error("재고 복구를 실패했습니다.");
            case "STOCK_DEDUCT_SUCCESS" ->
                    handleStockCheckSuccess(payEvent);
            case "STOCK_DEDUCT_FAILED" ->
                    handleStockCheckFailed(payEvent);
            default -> handleInvalidStatus(payEvent);
        }
    }

    private void handleStockCheckSuccess(PayEvent payEvent) {
        Pay pay = payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(payEvent.getUsername(), PayStatus.STOCK_DEDUCTED)
                .orElseThrow(() -> new RuntimeException("Pay not found"));

        if (shouldSimulateCustomerCancellation()) {
            handleCustomerCancellation(pay, payEvent);
        }
    }

    private boolean shouldSimulateCustomerCancellation() {
        return Math.random() < 0.2;
    }

    private void handleCustomerCancellation(Pay pay, PayEvent payEvent) {
        pay.setPayStatus(PayStatus.PAY_CANCEL);
        payRepository.save(pay);
        log.warn("고객 이탈 시나리오: 사용자 {}", pay.getUsername());

        PayEvent stockRecoverEvent = new PayEvent("STOCK_RECOVER", payEvent.getUsername(), payEvent.getItemId(),
                payEvent.getQuantity(), payEvent.getAmount(), 0);
        stockRecoverEvent.setEventId(UUID.randomUUID().toString());
        productProducer.sendProductEvent(stockRecoverEvent);
    }

    private void updateLastProcessedSequence(PayEvent payEvent) {
        String key = generateKey(payEvent);
        lastProcessedSequence.get(key).set(payEvent.getSequenceNumber());
    }

    private String generateKey(PayEvent payEvent) {
        return payEvent.getUsername() + ":" + payEvent.getItemId();
    }

    private void handleProcessingError(PayEvent payEvent) {
        log.error("Failed to process event: {}", payEvent);
    }

    private void handleInvalidStatus(PayEvent payEvent) {
        log.error("Invalid event status: {}", payEvent.getStatus());
    }

    private void handleStockCheckFailed(PayEvent payEvent) {
        Pay pay = payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(payEvent.getUsername(), PayStatus.PAY_START)
                .orElseGet(() -> payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(payEvent.getUsername(), PayStatus.STOCK_DEDUCTED)
                        .orElseThrow(() -> new RuntimeException("Pay not found")));
        pay.setPayStatus(PayStatus.PAY_FAILED);
        payRepository.save(pay);
        log.warn("재고 부족으로 결제 실패: 사용자 {}, 상품 ID {}", payEvent.getUsername(), payEvent.getItemId());
    }
}