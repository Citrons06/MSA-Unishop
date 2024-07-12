package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.ProcessEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessConsumer {

    private static final String PROCESS_TOPIC = "process-topic";
    private static final String PROCESS_GROUP_ID = "process-group";

    private final ProcessProducer processProducer;
    private final ConcurrentHashMap<String, AtomicLong> lastProcessedSequence = new ConcurrentHashMap<>();
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = PROCESS_TOPIC, groupId = PROCESS_GROUP_ID, containerFactory = "processEventKafkaListenerContainerFactory", concurrency = "3", batch = "true")
    public void consume(List<ProcessEvent> processEvents) {
        log.info("Consumed {} ProcessEvents", processEvents.size());

        for (ProcessEvent processEvent : processEvents) {
            if (processedEventIds.contains(processEvent.getEventId()) || !isValidSequence(processEvent)) {
                continue;
            }

            try {
                processEventByStatus(processEvent);
                processedEventIds.add(processEvent.getEventId());
                updateLastProcessedSequence(processEvent);
            } catch (Exception e) {
                log.error("Failed to process ProcessEvent", e);
                handleProcessingError(processEvent);
            }
        }
    }

    private boolean isValidSequence(ProcessEvent processEvent) {
        String key = processEvent.getUsername();
        long currentSequence = processEvent.getSequenceNumber();
        AtomicLong lastSequence = lastProcessedSequence.computeIfAbsent(key, k -> new AtomicLong(0));
        return currentSequence > lastSequence.get();
    }

    private void processEventByStatus(ProcessEvent processEvent) {
        switch (processEvent.getStatus()) {
            case "PAY_COMPLETE" -> processPayComplete(processEvent);
            default -> handleInvalidStatus(processEvent);
        }
    }

    @Transactional
    public void processPayComplete(ProcessEvent processEvent) {
        log.info("ORDER_CREATE 이벤트를 전송합니다. {}", processEvent);
        ProcessEvent orderCreateEvent = createOrderCreateEvent(processEvent);
        orderCreateEvent.setEventId(UUID.randomUUID().toString());
        processProducer.sendProcessEvent(orderCreateEvent);
    }

    private ProcessEvent createOrderCreateEvent(ProcessEvent processEvent) {
        return new ProcessEvent("ORDER_CREATE", processEvent.getUsername(), processEvent.getOrderUsername(),
                processEvent.getItemId(), processEvent.getQuantity(), processEvent.getAmount(),
                processEvent.getOrderAddress(), processEvent.getOrderTel());
    }

    private void updateLastProcessedSequence(ProcessEvent processEvent) {
        String key = processEvent.getUsername();
        lastProcessedSequence.get(key).set(processEvent.getSequenceNumber());
    }

    private void handleInvalidStatus(ProcessEvent processEvent) {
        log.error("Invalid event status: {}", processEvent.getStatus());
    }

    private void handleProcessingError(ProcessEvent processEvent) {
        log.error("Error processing event: {}", processEvent);
    }
}