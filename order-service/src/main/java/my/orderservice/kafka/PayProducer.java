package my.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.kafka.event.PayEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayProducer {

    private final KafkaTemplate<String, PayEvent> kafkaTemplate;

    private static final String TOPIC = "pay-topic";

    public void sendPayEvent(PayEvent payEvent) {
        try {
            // PayEvent 객체를 직렬화하여 Kafka 토픽에 전송
            kafkaTemplate.send(TOPIC, payEvent);
            log.info("Sent event: {}", payEvent);
        } catch (Exception e) {
            log.error("Error sending event", e);
        }
    }
}
