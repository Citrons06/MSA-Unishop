package my.productservice.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.inventory.service.InventoryService;
import my.productservice.item.dto.ItemResponseDto;
import my.productservice.item.service.ItemReadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/product/api")
@RequiredArgsConstructor
public class ItemApiController {

    private final ItemReadService itemReadService;
    private final InventoryService inventoryService;

    @GetMapping("/list")
    public ResponseEntity<?> getItems(@RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "8") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ItemResponseDto> items = itemReadService.getItems(pageRequest);
        items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<?> getItem(@PathVariable("itemId") Long itemId) {
        ItemResponseDto item = itemReadService.getItem(itemId);
        item.setQuantity(inventoryService.getStock(itemId));
        return ResponseEntity.ok().body(item);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchItems(@RequestParam(value = "search") String search,
                                         @RequestParam(value = "category", required = false) Long categoryId,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "8") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ItemResponseDto> items;
        if (categoryId != null) {
            items = itemReadService.searchItemsByCategoryAndItemName(categoryId, search, pageRequest);
            items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
        } else {
            items = itemReadService.searchItemsByName(search, pageRequest);
            items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
        }
        return ResponseEntity.ok().body(items);
    }
}