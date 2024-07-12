package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.PayEvent;
import my.payservice.pay.service.PayService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
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
    private final ConcurrentHashMap<String, AtomicLong> lastProcessedSequence = new ConcurrentHashMap<>();
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @KafkaListener(topics = PAY_TOPIC, groupId = PAY_GROUP_ID, containerFactory = "payEventKafkaListenerContainerFactory")
    public void consume(PayEvent payEvent) {
        log.info("Consumed event: {}", payEvent);

        if (processedEventIds.contains(payEvent.getEventId()) || !isValidSequence(payEvent)) {
            log.warn("Skipping already processed or invalid sequence event: {}", payEvent.getEventId());
            return;
        }

        try {
            processEvent(payEvent);
            processedEventIds.add(payEvent.getEventId());
            updateLastProcessedSequence(payEvent);
        } catch (Exception e) {
            log.error("Failed to process event: {}", payEvent, e);
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
            case "STOCK_DEDUCT_SYNCED"->
                    log.info("판매량을 동기화했습니다.");
            case "STOCK_DEDUCT_FAILED" ->
                    log.error("판매량 동기화에 실패했습니다.");
            case "PAY_FAILED", "PAY_COMPLETE" ->
                    payService.updatePaymentStatus(payEvent);
            case "STOCK_RECOVER_SUCCESS" ->
                    log.info("재고 복구를 성공했습니다.");
            case "STOCK_RECOVER_FAILED" ->
                    log.error("재고 복구를 실패했습니다.");
            default ->
                    log.error("Invalid event status: {}", payEvent.getStatus());

        }
    }

    private void updateLastProcessedSequence(PayEvent payEvent) {
        String key = generateKey(payEvent);
        lastProcessedSequence.get(key).set(payEvent.getSequenceNumber());
    }

    private String generateKey(PayEvent payEvent) {
        return payEvent.getUsername() + ":" + payEvent.getItemId();
    }
}