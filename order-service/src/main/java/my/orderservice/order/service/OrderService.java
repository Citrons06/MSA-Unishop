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

    // 주문 생성
    public OrderResponseDto order(String username, OrderRequestDto orderRequestDto) {
        // 회원, 상품 정보 가져오기
        UserDto member = userAdapter.getUserByUsername(username);
        ProductDto product = productAdapter.getProductById(orderRequestDto.getItemId());

        if (!"SELL".equals(product.getItemSellStatus())) {
            throw new IllegalArgumentException("해당 상품은 판매 중이 아닙니다.");
        }

        if (orderRequestDto.getQuantity() > product.getQuantity()) {
            throw new IllegalArgumentException("주문 수량이 재고 수량을 초과합니다.");
        }

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
        product.setQuantity(product.getQuantity() - orderRequestDto.getQuantity());

        orderRepository.save(order);

        // OrderResponseDto 생성
        return new OrderResponseDto(order);
    }

    // 주문 취소
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() == OrderStatus.CANCEL) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        if (order.getOrderStatus() == OrderStatus.DELIVERING) {
            throw new IllegalStateException("배송 중인 상품은 취소가 불가능합니다.");
        }
        if (LocalDateTime.now().isAfter(order.getOrderDate().plusDays(1))) {
            throw new IllegalStateException("주문 완료 후 1일 이내에만 취소가 가능합니다.");
        }

        // update CANCEL
        order.cancel();

        // 재고 증가
        for (OrderItem orderItem : order.getOrderItems()) {
            ProductDto product = productAdapter.getProductById(orderItem.getItemId());
            product.setQuantity(product.getQuantity() + orderItem.getCount());
        }
    }

    // 반품
    public void returnOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // 반품 조건을 충족하면 update RETURN_REQUESTED
        order.returnOrder();
    }

    // 반품 처리 되었던 상품 재고 회복
    @Scheduled(fixedRate = 60 * 1000) // 1분마다 실행
    public void updateStockQuantity() {
        orderRepository.findByOrderStatusAndReturnRequestDateBefore(OrderStatus.RETURN_REQUESTED, LocalDateTime.now().minusDays(3))
                .forEach(order -> {
                    order.completeReturn();
                    for (OrderItem orderItem : order.getOrderItems()) {
                        ProductDto product = productAdapter.getProductById(orderItem.getItemId());
                        product.setQuantity(product.getQuantity() + orderItem.getCount());
                    }
                    order.completeReturn();
                    orderRepository.save(order);
                });
    }

    // 주문 상태 업데이트
    @Scheduled(fixedRate = 60 * 1000) // 1분마다 실행
    public void updateOrderStatus() {
        List<Order> orders = orderRepository.findByOrderStatus(OrderStatus.ORDERED);

        for (Order order : orders) {
            if (order.getOrderDate().plusDays(1).isBefore(LocalDateTime.now())) {
                order.setOrderStatus(OrderStatus.DELIVERING);
            }
            if (order.getOrderDate().plusDays(2).isBefore(LocalDateTime.now())) {
                order.completeDelivery();
            }
            orderRepository.save(order);
        }
    }

    public List<OrderResponseDto> getOrderList(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByOrderUsername(username, pageable);

        orderPage.forEach(order -> {
            order.getOrderItems().forEach(orderItem -> {
                ProductDto product = productAdapter.getProductById(orderItem.getItemId());
                orderItem.setItemName(product.getItemName());
                orderItem.setOrderPrice(product.getPrice() * product.getQuantity());
            });
        });

        return orderPage.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }

    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        order.getOrderItems().forEach(orderItem -> {
            ProductDto product = productAdapter.getProductById(orderItem.getItemId());
            orderItem.setItemName(product.getItemName());
            orderItem.setOrderPrice(product.getPrice() * product.getQuantity());
        });

        return new OrderResponseDto(order);
    }
}