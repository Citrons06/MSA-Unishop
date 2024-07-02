package my.productservice.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.admin.service.CategoryAdminService;
import my.productservice.inventory.service.InventoryService;
import my.productservice.item.dto.CategoryResponseDto;
import my.productservice.item.service.ItemReadService;
import my.productservice.item.dto.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemReadService itemService;
    private final InventoryService inventoryService;
    private final CategoryAdminService categoryAdminService;

    @GetMapping("/item/list")
    public String itemList(@RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "category", required = false) Long categoryId,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "8") int size,
                           Model model) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ItemResponseDto> items;
        List<CategoryResponseDto> categories = categoryAdminService.getCategories();

        if (search != null && !search.isEmpty()) {
            if (categoryId != null) {
                items = itemService.searchItemsByCategoryAndItemName(categoryId, search, pageRequest);
                items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
            } else {
                items = itemService.searchItemsByName(search, pageRequest);
                items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
            }
        } else {
            if (categoryId != null) {
                items = itemService.getItemsByCategory(categoryId, pageRequest);
                items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
            } else {
                items = itemService.getItems(pageRequest);
                items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
            }
        }
        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        return "items/itemList";
    }

    @GetMapping("/item/{itemId}")
    public String getItem(Model model, @PathVariable Long itemId) {
        ItemResponseDto item = itemService.getItem(itemId);
        item.setQuantity(inventoryService.getStock(itemId));
        model.addAttribute("item", item);
        return "items/itemDetail";
    }

    // 상품 상세 화면에서 바로 주문하기
    @GetMapping("/item/order")
    public String orderFromItem(Model model, Long itemId) {
        ItemResponseDto item = itemService.getItem(itemId);
        item.setQuantity(inventoryService.getStock(itemId));
        model.addAttribute("item", item);
        return "order/orderConfirm";
    }
}