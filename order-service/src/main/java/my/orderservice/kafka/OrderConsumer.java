package my.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.kafka.event.OrderEvent;
import my.orderservice.kafka.event.PayEvent;
import my.orderservice.order.entity.Order;
import my.orderservice.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private static final String ORDER_TOPIC = "pay-topic";
    private static final String ORDER_GROUP_ID = "order-group";

    private final OrderRepository orderRepository;

    @KafkaListener(topics = ORDER_TOPIC, groupId = ORDER_GROUP_ID)
    public void consume(PayEvent payEvent) {
        log.info("Consumed event: {}", payEvent);

        try {
            switch (payEvent.getStatus()) {
                case "ORDER_CREATE":
                    createOrder(payEvent);
                    break;
                default:
                    log.error("Invalid event status: {}", payEvent.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }
    }

    @Transactional
    public void createOrder(PayEvent payEvent) {
        Order order = Order.builder()
                .orderPrice(payEvent.getAmount())
                .orderUsername(payEvent.getUsername())
                .build();
        orderRepository.save(order);
    }
}