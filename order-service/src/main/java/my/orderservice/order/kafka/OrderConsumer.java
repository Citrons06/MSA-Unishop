package my.orderservice.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.order.dto.OrderRequestDto;
import my.orderservice.order.entity.Order;
import my.orderservice.order.entity.OrderItem;
import my.orderservice.order.entity.OrderStatus;
import my.orderservice.order.kafka.event.OrderEvent;
import my.orderservice.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void consume(OrderEvent orderEvent) {
        log.info("Consumed event: {}", orderEvent);

        try {
            if ("ORDER_CREATED".equals(orderEvent.getStatus())) {
                createOrder(orderEvent);

                // 재고 업데이트 이벤트 발행
                OrderEvent stockEvent = new OrderEvent(
                        "STOCK_UPDATED", orderEvent.getMemberId(), orderEvent.getItemId(), orderEvent.getItemName(),
                        orderEvent.getCity(), orderEvent.getStreet(), orderEvent.getZipcode(),
                        orderEvent.getOrderTel(), orderEvent.getOrderUsername(), orderEvent.getQuantity(), orderEvent.getOrderPrice()
                );
                orderProducer.sendOrderEvent(stockEvent);
            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }
    }

    @Transactional
    public void createOrder(OrderEvent orderEvent) {

        // 주문 생성
        Order order = Order.builder()
                .memberId(orderEvent.getMemberId())
                .orderUsername(orderEvent.getOrderUsername())
                .orderAddress(orderEvent.getCity() + " " + orderEvent.getStreet() + " " + orderEvent.getZipcode())
                .orderTel(orderEvent.getOrderTel())
                .orderStatus(OrderStatus.ORDERED)
                .orderPrice(orderEvent.getOrderPrice())
                .build();

        // 주문 항목 생성 및 추가
        OrderItem orderItem = new OrderItem(order, orderEvent.getItemId(), orderEvent.getItemName(), orderEvent.getQuantity(), orderEvent.getOrderPrice());
        order.getOrderItems().add(orderItem);

        orderRepository.save(order);

        log.info("주문이 완료되었습니다.: {}", order);
    }
}
