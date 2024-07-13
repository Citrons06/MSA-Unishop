package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.PayEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductProducer {

    private static final String TOPIC = "product-topic";
    private final KafkaTemplate<String, PayEvent> kafkaTemplate;

    private final ConcurrentHashMap<String, AtomicLong> sequenceNumbers = new ConcurrentHashMap<>();

    @Transactional
    public void sendProductEvent(PayEvent payEvent) {
        try {
            String key = payEvent.getUsername();
            long sequenceNumber = sequenceNumbers.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            payEvent.setSequenceNumber(sequenceNumber);

            SendResult<String, PayEvent> result = kafkaTemplate.send(TOPIC, key, payEvent).get();
            log.info("Sent product event: {}, offset: {}", payEvent, result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Error sending product event", e);
            throw new RuntimeException("Failed to send product event", e);
        }
    }
}