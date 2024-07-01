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
                        orderEvent.getItemId(), orderEvent.getMemberId(),
                        orderEvent.getQuantity(), "STOCK_UPDATED",
                        orderEvent.getUsername(), orderEvent.getOrderRequestDto(),
                        orderEvent.getItemName());
                orderProducer.sendOrderEvent(stockEvent);
            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }
    }

    @Transactional
    public void createOrder(OrderEvent orderEvent) {
        OrderRequestDto orderRequestDto = orderEvent.getOrderRequestDto();

        // 주문 생성
        Order order = Order.builder()
                .memberId(orderEvent.getMemberId())
                .orderUsername(orderRequestDto.getOrderUsername())
                .orderAddress(orderRequestDto.getCity() + " " + orderRequestDto.getStreet() + " " + orderRequestDto.getZipcode())
                .orderTel(orderRequestDto.getOrderTel())
                .orderStatus(OrderStatus.ORDERED)
                .build();

        // 주문 항목 생성 및 추가
        OrderItem orderItem = new OrderItem(order, orderEvent.getItemId(), orderEvent.getItemName(), orderRequestDto.getQuantity(), 0);
        order.getOrderItems().add(orderItem);

        // 총 금액 설정
        order.setOrderPrice(order.getTotalPrice());

        orderRepository.save(order);

        log.info("주문이 완료되었습니다.: {}", order);
    }
}
