package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.ProcessEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessConsumer {

    private static final String PROCESS_TOPIC = "process-topic";
    private static final String PROCESS_GROUP_ID = "process-group";

    private final PayProducer payProducer;

    @KafkaListener(topics = PROCESS_TOPIC, groupId = PROCESS_GROUP_ID, containerFactory = "processEventKafkaListenerContainerFactory")
    public void consume(ProcessEvent processEvent) {
        log.info("Consumed ProcessEvent: {}", processEvent);

        try {
            if (processEvent.getStatus().equals("PAY_COMPLETE")) {
                processPayComplete(processEvent);
            } else {
                log.error("Invalid event status: {}", processEvent.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to process ProcessEvent", e);
        }
    }

    @Transactional
    public void processPayComplete(ProcessEvent processEvent) {
        ProcessEvent orderCreateEvent = new ProcessEvent("ORDER_CREATE", processEvent.getUsername(), processEvent.getOrderUsername(), processEvent.getItemId(), processEvent.getQuantity(), processEvent.getAmount(), processEvent.getOrderAddress(), processEvent.getOrderTel());
        payProducer.sendProcessEvent(orderCreateEvent);
    }
}
