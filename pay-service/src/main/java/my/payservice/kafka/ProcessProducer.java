package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.ProcessEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessProducer {

    private final KafkaTemplate<String, ProcessEvent> processEventKafkaTemplate;

    private static final String PROCESS_TOPIC = "process-topic";

    private final ConcurrentHashMap<String, AtomicLong> sequenceNumbers = new ConcurrentHashMap<>();

    @Transactional
    public void sendProcessEvent(ProcessEvent processEvent) {
        try {
            String key = processEvent.getUsername() + ":" + processEvent.getItemId();
            long sequenceNumber = sequenceNumbers.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            processEvent.setSequenceNumber(sequenceNumber);
            processEvent.setEventId(UUID.randomUUID().toString()); // 고유 식별자 추가

            SendResult<String, ProcessEvent> result = processEventKafkaTemplate.send(PROCESS_TOPIC, key, processEvent).get();
            log.info("Sent ProcessEvent: {}, offset: {}", processEvent, result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Error sending ProcessEvent", e);
            throw new RuntimeException("Failed to send ProcessEvent", e);
        }
    }
}