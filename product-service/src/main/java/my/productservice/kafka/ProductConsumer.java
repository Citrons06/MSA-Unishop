package my.productservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.item.service.ItemWriteService;
import my.productservice.kafka.event.PayEvent;
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
@RequiredArgsConstructor
public class ProductConsumer {

    private static final String PAY_TOPIC = "product-topic";
    private static final String PRODUCT_GROUP_ID = "product-group";

    private final ItemWriteService itemWriteService;
    private final PayProducer payProducer;
    private final ConcurrentHashMap<String, AtomicLong> lastProcessedSequence = new ConcurrentHashMap<>();
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @KafkaListener(topics = PAY_TOPIC, groupId = PRODUCT_GROUP_ID, containerFactory = "payKafkaListenerContainerFactory")
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
            PayEvent resultEvent = processEvent(payEvent);
            if (resultEvent != null) {
                resultEvent.setEventId(UUID.randomUUID().toString());
                payProducer.sendProductEvent(resultEvent);
            }
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
    public PayEvent processEvent(PayEvent payEvent) {
        return switch (payEvent.getStatus()) {
            case "STOCK_DEDUCT" -> handleStockDeduct(payEvent);
            case "STOCK_RECOVER" -> handleStockRecover(payEvent);
            default -> {
                log.error("Invalid event status: {}", payEvent.getStatus());
                yield null;
            }
        };
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PayEvent handleStockDeduct(PayEvent payEvent) {
        try {
            boolean success = itemWriteService.updateQuantityAndSellCount(payEvent.getItemId(), -payEvent.getQuantity());
            String status = success ? "STOCK_DEDUCT_SUCCESS" : "STOCK_DEDUCT_FAILED";
            log.info("{}: 사용자 {}, 상품 ID {}, 수량 {}", status, payEvent.getUsername(), payEvent.getItemId(), payEvent.getQuantity());
            return new PayEvent(status, payEvent.getUsername(), payEvent.getItemId(),
                    payEvent.getQuantity(), payEvent.getAmount(), 0);
        } catch (Exception e) {
            log.error("Error in handleStockDeduct: ", e);
            return new PayEvent("STOCK_DEDUCT_FAILED", payEvent.getUsername(), payEvent.getItemId(),
                    payEvent.getQuantity(), payEvent.getAmount(), 0);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PayEvent handleStockRecover(PayEvent payEvent) {
        boolean success = itemWriteService.updateQuantityAndSellCount(payEvent.getItemId(), payEvent.getQuantity());
        String status = success ? "STOCK_RECOVER_SUCCESS" : "STOCK_RECOVER_FAILED";
        log.info("{}: 사용자 {}, 상품 ID {}, 수량 {}", status, payEvent.getUsername(), payEvent.getItemId(), payEvent.getQuantity());
        return new PayEvent(status, payEvent.getUsername(), payEvent.getItemId(),
                payEvent.getQuantity(), payEvent.getAmount(), 0);
    }

    private void updateLastProcessedSequence(PayEvent payEvent) {
        String key = generateKey(payEvent);
        lastProcessedSequence.get(key).set(payEvent.getSequenceNumber());
    }

    private String generateKey(PayEvent payEvent) {
        return payEvent.getUsername() + ":" + payEvent.getItemId();
    }

    private void handleProcessingError(PayEvent payEvent) {
        log.error("Error processing event: {}", payEvent);
    }
}