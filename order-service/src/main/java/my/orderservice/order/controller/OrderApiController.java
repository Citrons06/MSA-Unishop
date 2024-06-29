package my.orderservice.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.order.dto.OrderRequestDto;
import my.orderservice.order.dto.OrderResponseDto;
import my.orderservice.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;

    // 주문 전체 내역 조회
    @GetMapping("/list")
    public ResponseEntity<?> orderList(HttpServletRequest request,
                                       @RequestParam(name = "page", defaultValue = "0") int page,
                                       @RequestParam(name = "size", defaultValue = "10") int size) {
        String username = request.getHeader("X-User-Name");
        List<OrderResponseDto> orders = orderService.getOrderList(username, page, size);
        return ResponseEntity.ok(orders);
    }

    // 주문 단건 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<?> orderDetail(@PathVariable("orderId") Long orderId) {
        OrderResponseDto order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(HttpServletRequest request, @RequestBody OrderRequestDto orderRequestDto) {
        String username = request.getHeader("X-User-Name");
        OrderResponseDto order = orderService.order(username, orderRequestDto);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }

    @PostMapping("/return/{orderId}")
    public ResponseEntity<String> returnOrder(@PathVariable("orderId") Long orderId) {
        orderService.returnOrder(orderId);
        return ResponseEntity.ok("반품 신청을 완료하였습니다.");
    }
}
