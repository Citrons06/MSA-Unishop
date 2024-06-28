package my.userservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "orderClient", url = "${order-service.url}")
public interface OrderAdapter {

    @GetMapping("/api/internal/order/list")
    OrderDto orderList(Long memberId);
}
