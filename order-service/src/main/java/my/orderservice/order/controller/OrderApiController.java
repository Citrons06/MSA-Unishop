package my.orderservice.order.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.exception.CommonException;
import my.orderservice.exception.ErrorCode;
import my.orderservice.order.dto.CreateOrderResponse;
import my.orderservice.order.dto.OrderResponseDto;
import my.orderservice.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order/api")
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
        CreateOrderResponse oneOrder = orderService.findOneOrder(orderId);
        return ResponseEntity.ok(oneOrder);
    }

    // 주문 취소
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("주문이 취소되었습니다.");
        } catch (Exception e) {
            throw new CommonException(ErrorCode.CANCEL_FAILED);
        }
    }

    // 반품 신청
    @PostMapping("/return/{orderId}")
    public ResponseEntity<String> returnOrder(@PathVariable("orderId") Long orderId) {
        try {
            orderService.returnOrder(orderId);
            return ResponseEntity.ok("반품 신청을 완료하였습니다.");
        } catch (Exception e) {
            throw new CommonException(ErrorCode.RETURN_FAILED);
        }
    }
}
