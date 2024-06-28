package my.orderservice.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.adapter.ProductAdapter;
import my.orderservice.adapter.ProductDto;
import my.orderservice.adapter.UserAdapter;
import my.orderservice.adapter.UserDto;
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
    private final UserAdapter userAdapter;

    private static final int CANCELATION_PERIOD_DAYS = 1;
    private static final int DELIVERY_PERIOD_DAYS = 1;
    private static final int DELIVERY_COMPLETION_PERIOD_DAYS = 2;
    private static final int RETURN_REQUEST_PERIOD_DAYS = 3;

    public OrderResponseDto order(String username, OrderRequestDto orderRequestDto) {
        // 회원, 상품 정보 가져오기
        UserDto member = userAdapter.getUserByUsername(username);
        ProductDto product = productAdapter.getItem(orderRequestDto.getItemId());

        validateProductAvailability(product, orderRequestDto.getQuantity());

        // 주문 생성
        Order order = Order.builder()
                .memberId(member.getId())
                .orderUsername(username)
                .orderAddress(orderRequestDto.getCity() + " " + orderRequestDto.getStreet() + " " + orderRequestDto.getZipcode())
                .orderTel(orderRequestDto.getOrderTel())
                .orderStatus(OrderStatus.ORDERED)
                .build();

        // 주문 항목 생성 및 추가
        OrderItem orderItem = new OrderItem(order, product.getId(), product.getItemName(), orderRequestDto.getQuantity(), product.getPrice());
        order.getOrderItems().add(orderItem);

        // 총 금액 설정
        order.setOrderPrice(order.getTotalPrice());

        // 재고 감소
        adjustStockQuantity(order.getOrderItems(), -1);

        orderRepository.save(order);

        log.info("주문이 완료되었습니다.: {}", order);

        // OrderResponseDto 생성
        return new OrderResponseDto(order);
    }

    public void cancelOrder(Long orderId) {
        Order order = getOrderByIdOrThrow(orderId);

        validateCancellation(order);

        // update CANCEL
        order.cancel();

        // 재고 증가
        adjustStockQuantity(order.getOrderItems(), 1);

        orderRepository.save(order);

        log.info("해당 주문이 취소되었습니다.: {}", order);
    }

    public void returnOrder(Long orderId) {
        Order order = getOrderByIdOrThrow(orderId);

        // 반품 조건을 충족하면 update RETURN_REQUESTED
        order.returnOrder();
        orderRepository.save(order);

        log.info("해당 주문이 반품 신청되었습니다.: {}", order);
    }

    @Scheduled(fixedRate = 60 * 1000) // 1분마다 실행
    public void updateStockQuantity() {
        List<Order> orders = orderRepository.findByOrderStatusAndReturnRequestDateBefore(OrderStatus.RETURN_REQUESTED, LocalDateTime.now().minusDays(RETURN_REQUEST_PERIOD_DAYS));
        orders.forEach(order -> {
            order.completeReturn();
            adjustStockQuantity(order.getOrderItems(), 1);
            orderRepository.save(order);

            log.info("반품된 상품의 재고가 회복됩니다.: {}", order);
        });
    }

    @Scheduled(fixedRate = 60 * 1000) // 1분마다 실행
    public void updateOrderStatus() {
        List<Order> orders = orderRepository.findByOrderStatus(OrderStatus.ORDERED);

        orders.forEach(order -> {
            updateStatusBasedOnDate(order);
            orderRepository.save(order);

            log.info("해당 주문의 상태가 변경됩니다.: {}", order);
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
    }

    private void validateProductAvailability(ProductDto product, int orderQuantity) {
        if (!"SELL".equals(product.getItemSellStatus())) {
            throw new IllegalArgumentException("해당 상품은 판매 중이 아닙니다.");
        }
        if (orderQuantity > product.getQuantity()) {
            throw new IllegalArgumentException("주문 수량이 재고 수량을 초과합니다.");
        }
    }

    private void validateCancellation(Order order) {
        if (order.getOrderStatus() == OrderStatus.CANCEL) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        if (order.getOrderStatus() == OrderStatus.DELIVERING) {
            throw new IllegalStateException("배송 중인 상품은 취소가 불가능합니다.");
        }
        if (LocalDateTime.now().isAfter(order.getOrderDate().plusDays(CANCELATION_PERIOD_DAYS))) {
            throw new IllegalStateException("주문 완료 후 1일 이내에만 취소가 가능합니다.");
        }
    }

    private void adjustStockQuantity(List<OrderItem> orderItems, int multiplier) {
        for (OrderItem orderItem : orderItems) {
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