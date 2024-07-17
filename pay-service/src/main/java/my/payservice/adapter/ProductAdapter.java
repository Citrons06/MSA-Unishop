package my.payservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service")
public interface ProductAdapter {

    @GetMapping("/product/api/internal/sold-time/{itemId}")
    SoldTimeDto getSoldTime(Long itemId);
}
