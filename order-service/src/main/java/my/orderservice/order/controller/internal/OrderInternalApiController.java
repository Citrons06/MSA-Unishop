package my.orderservice.order.controller.internal;

import lombok.RequiredArgsConstructor;
import my.orderservice.order.dto.OrderResponseDto;
import my.orderservice.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderInternalApiController {

    private final OrderService orderService;

    @GetMapping("/api/order/internal/list")
    public List<OrderResponseDto> orderList(@RequestParam("username") String username) {
        return orderService.getOrderList(username, 10, 10);
    }
}
