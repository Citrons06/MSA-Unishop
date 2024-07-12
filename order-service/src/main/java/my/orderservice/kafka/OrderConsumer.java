package my.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.adapter.UserAdapter;
import my.orderservice.kafka.event.ProcessEvent;
import my.orderservice.order.entity.Order;
import my.orderservice.order.entity.OrderStatus;
import my.orderservice.order.repository.OrderRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {
    private static final String ORDER_TOPIC = "process-topic";
    private static final String ORDER_GROUP_ID = "order-group";

    private final OrderRepository orderRepository;
    private final UserAdapter userAdapter;
    private final ConcurrentHashMap<String, AtomicLong> lastProcessedSequence = new ConcurrentHashMap<>();
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @KafkaListener(topics = ORDER_TOPIC, groupId = ORDER_GROUP_ID, containerFactory = "processKafkaListenerContainerFactory", concurrency = "3", batch = "true")
    public void consume(List<ProcessEvent> events) {
        log.info("Consumed {} events", events.size());

        List<Order> ordersToSave = new ArrayList<>();

        for (ProcessEvent processEvent : events) {
            if (processedEventIds.contains(processEvent.getEventId()) || !isValidSequence(processEvent)) {
                continue;
            }

            try {
                if (processEvent.getStatus().equals("ORDER_CREATE")) {
                    Order order = createOrder(processEvent);
                    ordersToSave.add(order);
                } else {
                    log.error("Invalid event status: {}", processEvent.getStatus());
                }

                processedEventIds.add(processEvent.getEventId());
                updateLastProcessedSequence(processEvent);
            } catch (Exception e) {
                log.error("Failed to process event", e);
                handleProcessingError(processEvent);
            }
        }

        if (!ordersToSave.isEmpty()) {
            orderRepository.saveAll(ordersToSave);
            log.info("Saved {} orders", ordersToSave.size());
        }
    }

    private boolean isValidSequence(ProcessEvent processEvent) {
        String key = processEvent.getUsername();
        long currentSequence = processEvent.getSequenceNumber();
        AtomicLong lastSequence = lastProcessedSequence.computeIfAbsent(key, k -> new AtomicLong(0));
        return currentSequence > lastSequence.get();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Order createOrder(ProcessEvent processEvent) {
        log.info("주문이 생성됩니다. 사용자: {}, 상품ID: {}", processEvent.getOrderUsername(), processEvent.getItemId());
        try {
            Long memberId = getUserIdByUsername(processEvent.getUsername());
            Order order = Order.builder()
                    .orderUsername(processEvent.getOrderUsername())
                    .orderAddress(processEvent.getOrderAddress())
                    .orderPrice(processEvent.getAmount())
                    .memberId(memberId)
                    .orderTel(processEvent.getOrderTel())
                    .orderStatus(OrderStatus.ORDER_COMPLETE)
                    .build();

            log.info("주문이 성공적으로 생성되었습니다. 주문자: {}", processEvent.getOrderUsername());
            return order;
        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생: ", e);
            throw e;
        }
    }

    private void updateLastProcessedSequence(ProcessEvent processEvent) {
        String key = processEvent.getUsername();
        lastProcessedSequence.get(key).set(processEvent.getSequenceNumber());
    }

    private void handleProcessingError(ProcessEvent processEvent) {
        log.error("Error processing event: {}", processEvent);
    }

    @Cacheable(value = "userIds", key = "#username")
    public Long getUserIdByUsername(String username) {
        return userAdapter.getUserByUsername(username).getId();
    }
}