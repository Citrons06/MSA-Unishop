package my.orderservice.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.order.kafka.event.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final String TOPIC = "order-topic";

    public void sendOrderEvent(OrderEvent orderEvent) {
        try {
            // OrderEvent 객체를 직렬화하여 Kafka 토픽에 전송
            kafkaTemplate.send(TOPIC, orderEvent);
            log.info("Sent event: {}", orderEvent);
        } catch (Exception e) {
            log.error("Error sending event", e);
        }
    }
}