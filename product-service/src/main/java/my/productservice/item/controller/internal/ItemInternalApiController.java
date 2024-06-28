package my.productservice.item.controller.internal;

import lombok.RequiredArgsConstructor;
import my.productservice.item.dto.ItemInternalResponse;
import my.productservice.item.dto.ItemResponseDto;
import my.productservice.item.service.ItemService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ItemInternalApiController {

    private final ItemService itemService;

    @GetMapping("/api/internal/product/{itemId}")
    public ItemInternalResponse getItem(@PathVariable("itemId") Long itemId) {
        ItemResponseDto item = itemService.getItem(itemId);
        return new ItemInternalResponse(item);
    }

    @PatchMapping("/api/internal/product/update-quantity/{itemId}")
    public ItemInternalResponse updateQuantity(@PathVariable("itemId") Long itemId, int quantity) {
        ItemResponseDto item = itemService.updateQuantity(itemId, quantity);
        itemService.updateItemSellCount(itemId, item.getQuantity());
        return new ItemInternalResponse(item);
    }
}