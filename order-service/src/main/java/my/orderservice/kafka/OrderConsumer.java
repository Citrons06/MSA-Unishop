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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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
    @KafkaListener(topics = ORDER_TOPIC, groupId = ORDER_GROUP_ID, containerFactory = "processKafkaListenerContainerFactory", concurrency = "3")
    public void consume(ProcessEvent processEvent) {
        log.info("Consumed event: {}", processEvent);

        if (processedEventIds.contains(processEvent.getEventId()) || !isValidSequence(processEvent)) {
            return;
        }

        try {
            if (processEvent.getStatus().equals("ORDER_CREATE")) {
                createOrder(processEvent);
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

    private boolean isValidSequence(ProcessEvent processEvent) {
        String key = processEvent.getUsername();
        long currentSequence = processEvent.getSequenceNumber();
        AtomicLong lastSequence = lastProcessedSequence.computeIfAbsent(key, k -> new AtomicLong(0));
        return currentSequence > lastSequence.get();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createOrder(ProcessEvent processEvent) {
        log.info("주문이 생성됩니다. 사용자: {}, 상품ID: {}", processEvent.getOrderUsername(), processEvent.getItemId());
        try {
            Order order = Order.builder()
                    .orderUsername(processEvent.getOrderUsername())
                    .orderAddress(processEvent.getOrderAddress())
                    .orderPrice(processEvent.getAmount())
                    .memberId(userAdapter.getUserByUsername(processEvent.getUsername()).getId())
                    .orderTel(processEvent.getOrderTel())
                    .orderStatus(OrderStatus.ORDER_COMPLETE)
                    .build();

            orderRepository.save(order);
            log.info("주문이 성공적으로 처리되었습니다. 주문자: {}, 주문번호: {}", processEvent.getOrderUsername(), order.getId());
        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생, 트랜잭션 롤백: ", e);
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
}