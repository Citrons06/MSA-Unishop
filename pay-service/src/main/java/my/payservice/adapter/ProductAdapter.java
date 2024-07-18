package my.payservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductAdapter {

    @GetMapping("/product/api/internal/sold-time/{itemId}")
    SoldTimeDto getSoldTime(@PathVariable("itemId") Long itemId);
}
