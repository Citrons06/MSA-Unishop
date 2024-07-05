package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.PayEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductProducer {

    private static final String TOPIC = "product-topic";
    private final KafkaTemplate<String, PayEvent> kafkaTemplate;

    public void sendProductEvent(PayEvent payEvent) {
        try {
            kafkaTemplate.send(TOPIC, payEvent);
            log.info("Sent event: {}", payEvent);
        } catch (Exception e) {
            log.error("Error sending event", e);
        }
    }
}
