package my.userservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "productClient", url = "${product-service.url}")
public interface ProductAdapter {

    @GetMapping("/api/internal/product/{itemId}")
    ProductDto getItem(@PathVariable("itemId") Long itemId);
}