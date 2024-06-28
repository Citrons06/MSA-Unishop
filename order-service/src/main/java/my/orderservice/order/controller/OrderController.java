package my.orderservice.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.order.dto.OrderResponseDto;
import my.orderservice.order.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 내역
    @GetMapping("/order/list")
    public String orderList(HttpServletRequest request, Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        String username = request.getHeader("X-User-Name");
        List<OrderResponseDto> orders = orderService.getOrderList(username, page, size);
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        return "order/orderlist";
    }

    // 주문 단건 상세 조회
    @GetMapping("/order/detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        OrderResponseDto order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "order/orderDetail";
    }
}