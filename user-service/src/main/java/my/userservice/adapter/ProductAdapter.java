package my.userservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductAdapter {

    @GetMapping("/api/product/internal")
    ProductDto getItem(@RequestParam("itemId") Long itemId);
}