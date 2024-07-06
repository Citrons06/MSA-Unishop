package my.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.adapter.UserAdapter;
import my.orderservice.kafka.event.ProcessEvent;
import my.orderservice.order.entity.Order;
import my.orderservice.order.entity.OrderStatus;
import my.orderservice.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private static final String ORDER_TOPIC = "process-topic";
    private static final String ORDER_GROUP_ID = "order-group";

    private final OrderRepository orderRepository;
    private final UserAdapter userAdapter;

    @KafkaListener(topics = ORDER_TOPIC, groupId = ORDER_GROUP_ID)
    public void consume(ProcessEvent processEvent) {
        log.info("Consumed event: {}", processEvent);

        try {
            if (processEvent.getStatus().equals("ORDER_CREATE")) {
                createOrder(processEvent);
            } else {
                log.error("Invalid event status: {}", processEvent.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }
    }

    @Transactional
    public void createOrder(ProcessEvent processEvent) {
        Order order = Order.builder()
                .orderUsername(processEvent.getOrderUsername())
                .orderAddress(processEvent.getOrderAddress())
                .orderPrice(processEvent.getAmount())
                .memberId(userAdapter.getUserByUsername(processEvent.getUsername()).getId())
                .orderTel(processEvent.getOrderTel())
                .orderStatus(OrderStatus.ORDER_COMPLETE)
                .build();
        orderRepository.save(order);
    }
}