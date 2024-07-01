package my.productservice.item.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.item.kafka.event.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final String TOPIC = "product-topic";

    public void sendProductEvent(OrderEvent productEvent) {
        log.info("Sending product event: {}", productEvent);
        kafkaTemplate.send(TOPIC, productEvent);
    }
}
