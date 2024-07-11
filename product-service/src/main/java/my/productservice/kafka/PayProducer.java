package my.productservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.kafka.event.PayEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayProducer {

    private final KafkaTemplate<String, PayEvent> kafkaTemplate;

    private static final String TOPIC = "pay-topic";

    private final ConcurrentHashMap<String, AtomicLong> sequenceNumbers = new ConcurrentHashMap<>();

    @Transactional
    public void sendProductEvent(PayEvent payEvent) {
        String key = payEvent.getUsername();
        long sequenceNumber = sequenceNumbers.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        payEvent.setSequenceNumber(sequenceNumber);

        kafkaTemplate.executeInTransaction(operations -> {
            operations.send(TOPIC, key, payEvent);
            return true;
        });
    }
}