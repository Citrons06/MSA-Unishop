package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.PayEvent;
import my.payservice.kafka.event.ProcessEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayProducer {

    private final KafkaTemplate<String, PayEvent> payEventKafkaTemplate;
    private final KafkaTemplate<String, ProcessEvent> processEventKafkaTemplate;

    private static final String PAY_TOPIC = "pay-topic";
    private static final String PROCESS_TOPIC = "process-topic";

    public void sendPayEvent(PayEvent payEvent) {
        try {
            payEventKafkaTemplate.send(PAY_TOPIC, payEvent);
            log.info("Sent PayEvent: {}", payEvent);
        } catch (Exception e) {
            log.error("Error sending PayEvent", e);
        }
    }

    public void sendProcessEvent(ProcessEvent processEvent) {
        try {
            processEventKafkaTemplate.send(PROCESS_TOPIC, processEvent);
            log.info("Sent ProcessEvent: {}", processEvent);
        } catch (Exception e) {
            log.error("Error sending ProcessEvent", e);
        }
    }
}