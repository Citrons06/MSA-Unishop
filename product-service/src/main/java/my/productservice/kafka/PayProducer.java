package my.productservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.kafka.event.PayEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayProducer {

    private final KafkaTemplate<String, PayEvent> kafkaTemplate;

    private static final String TOPIC = "pay-topic";

    public void sendProductEvent(PayEvent payEvent) {
        log.info("Sending product event: {}", payEvent);
        kafkaTemplate.send(TOPIC, payEvent);
    }
}
