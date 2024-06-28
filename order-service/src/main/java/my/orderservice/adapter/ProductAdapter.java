package my.orderservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "productClient", url = "${product-service.url}")
public interface ProductAdapter {

    @GetMapping("/api/internal/product/{itemId}")
    ProductDto getItem(@PathVariable("itemId") Long itemId);

    @PatchMapping("/api/internal/product/update-quantity/{itemId}")
    void updateQuantity(@PathVariable("itemId") Long itemId, int quantity);
}
