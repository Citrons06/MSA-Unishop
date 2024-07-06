package my.orderservice.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.adapter.ProductAdapter;
import my.orderservice.adapter.ProductDto;
import my.orderservice.exception.CommonException;
import my.orderservice.exception.ErrorCode;
import my.orderservice.order.dto.OrderRequestDto;
import my.orderservice.order.dto.OrderResponseDto;
import my.orderservice.order.entity.Order;
import my.orderservice.order.entity.OrderItem;
import my.orderservice.order.entity.OrderStatus;
import my.orderservice.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductAdapter productAdapter;

    private static final int CANCELATION_PERIOD_DAYS = 1;  // 주문 완료 후 1일 이내 취소 가능
    private static final int DELIVERY_PERIOD_DAYS = 1;  // 주문 완료 후 1일 이내 배송
    private static final int DELIVERY_COMPLETION_PERIOD_DAYS = 2;  // 주문 완료 후 2일 이내 배송 완료
    private static final int RETURN_REQUEST_PERIOD_DAYS = 3;  // 반품 신청 기간 3일

    // 결제 서버에서 결제에 성공한 것을 확인하면 주문 생성
    public OrderRequestDto order(String username, OrderRequestDto orderRequestDto) {
        return new OrderRequestDto();
    }

    public void cancelOrder(Long orderId) {
        Order order = getOrderByIdOrThrow(orderId);

        validateCancellation(order);

        // update CANCEL
        order.cancel();

        // 재고 증가
        adjustStockQuantity(order.getOrderItems(), 1);

        orderRepository.save(order);

        log.info("해당 주문이 취소되었습니다.: {}", order.getId());
    }

    public void returnOrder(Long orderId) {
        Order order = getOrderByIdOrThrow(orderId);

        // 반품 조건을 충족하면 update RETURN_REQUESTED
        order.returnOrder();
        orderRepository.save(order);

        log.info("해당 주문이 반품 신청되었습니다.: {}", order.getId());
    }

    @Scheduled(fixedRate = 60 * 1000) // 1분마다 실행
    public void updateStockQuantity() {
        List<Order> orders = orderRepository.findByOrderStatusAndReturnRequestDateBefore(OrderStatus.RETURN_REQUESTED, LocalDateTime.now().minusDays(RETURN_REQUEST_PERIOD_DAYS));
        orders.forEach(order -> {
            order.completeReturn();
            adjustStockQuantity(order.getOrderItems(), 1);
            orderRepository.save(order);

            log.info("반품된 상품의 재고가 회복됩니다.: {}", order.getOrderItems().stream().map(OrderItem::getItemId).collect(Collectors.toList()));
        });
    }

    @Scheduled(fixedRate = 60 * 30000) // 30분마다 실행
    @Transactional
    public void updateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliveryDateThreshold = now.minusDays(DELIVERY_PERIOD_DAYS);
        LocalDateTime deliveryCompletionDateThreshold = now.minusDays(DELIVERY_COMPLETION_PERIOD_DAYS);

        List<Order> orders = orderRepository.findOrdersForStatusUpdate(OrderStatus.ORDER_COMPLETE, deliveryDateThreshold, deliveryCompletionDateThreshold);

        orders.forEach(order -> {
            if (order.getOrderDate().isBefore(deliveryDateThreshold)) {
                order.setOrderStatus(OrderStatus.DELIVERING);
            }
            if (order.getOrderDate().isBefore(deliveryCompletionDateThreshold)) {
                order.setOrderStatus(OrderStatus.DELIVERED);
            }
            orderRepository.save(order);
            log.info(order.getId() + "번 주문 상태 업데이트: " + order.getOrderStatus());
        });
    }


    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrderList(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByOrderUsername(username, pageable);

        return orderPage.stream()
                .map(this::convertToOrderResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = getOrderByIdOrThrow(orderId);
        return convertToOrderResponseDto(order);
    }

    private Order getOrderByIdOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CommonException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void validateProductAvailability(ProductDto product, int orderQuantity) {
        if (!"SELL".equals(product.getItemSellStatus())) {
            throw new CommonException(ErrorCode.NOT_SELLING_PRODUCT);
        }
        if (orderQuantity > product.getQuantity()) {
            throw new CommonException(ErrorCode.OUT_OF_STOCK);
        }
    }

    private void validateCancellation(Order order) {
        if (order.getOrderStatus() == OrderStatus.CANCEL) {
            throw new CommonException(ErrorCode.ALREADY_CANCELED_ORDER);
        }
        if (order.getOrderStatus() == OrderStatus.DELIVERING) {
            throw new CommonException(ErrorCode.ALREADY_DELIVERING_ORDER);
        }
        if (LocalDateTime.now().isAfter(order.getOrderDate().plusDays(CANCELATION_PERIOD_DAYS))) {
            throw new CommonException(ErrorCode.CANCEL_PERIOD_EXPIRED);
        }
    }

    private void adjustStockQuantity(List<OrderItem> orderItems, int multiplier) {
        for (OrderItem orderItem : orderItems) {
            log.info("재고 조정: {}, {}", orderItem.getItemId(), multiplier * orderItem.getCount());
            productAdapter.updateQuantity(orderItem.getItemId(), multiplier * orderItem.getCount());
        }
    }

    private void updateStatusBasedOnDate(Order order) {
        if (order.getOrderDate().plusDays(DELIVERY_PERIOD_DAYS).isBefore(LocalDateTime.now())) {
            order.setOrderStatus(OrderStatus.DELIVERING);
        }
        if (order.getOrderDate().plusDays(DELIVERY_COMPLETION_PERIOD_DAYS).isBefore(LocalDateTime.now())) {
            order.setOrderStatus(OrderStatus.DELIVERED);
        }
    }

    private OrderResponseDto convertToOrderResponseDto(Order order) {
        order.getOrderItems().forEach(orderItem -> {
            ProductDto product = productAdapter.getItem(orderItem.getItemId());
            orderItem.setItemName(product.getItemName());
            orderItem.setOrderPrice(product.getPrice() * orderItem.getCount());
        });
        return new OrderResponseDto(order);
    }
}
