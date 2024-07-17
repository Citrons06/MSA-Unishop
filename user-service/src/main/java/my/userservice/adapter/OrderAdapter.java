package my.userservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderAdapter {

    @GetMapping("/order/api/internal/list")
    List<OrderDto> orderList(@RequestParam("username") String username);
}
