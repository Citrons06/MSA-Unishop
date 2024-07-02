package my.productservice.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.admin.service.CategoryAdminService;
import my.productservice.admin.service.ItemAdminService;
import my.productservice.inventory.dto.InventoryRequestDto;
import my.productservice.inventory.service.InventoryService;
import my.productservice.item.dto.CategoryResponseDto;
import my.productservice.item.dto.CreateItemResponse;
import my.productservice.item.dto.ItemRequestDto;
import my.productservice.item.dto.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/product/admin/item")
@RequiredArgsConstructor
public class ItemAdminController {

    private final ItemAdminService itemAdminService;
    private final CategoryAdminService categoryAdminService;
    private final InventoryService inventoryService;

    @GetMapping("/create")
    public String createItemForm(Model model) {
        model.addAttribute("item", new ItemRequestDto());
        model.addAttribute("quantity", new InventoryRequestDto());
        model.addAttribute("categories", categoryAdminService.getCategories());
        return "admin/items/createItemForm";
    }

    @GetMapping("/update/{itemId}")
    public String updateItemForm(@PathVariable Long itemId, Model model) {
        model.addAttribute("item", itemAdminService.getItem(itemId));
        model.addAttribute("quantity", inventoryService.getStock(itemId));
        model.addAttribute("categories", categoryAdminService.getCategories());
        return "admin/items/updateItemForm";
    }

    @GetMapping("/list")
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
                items = itemAdminService.searchItemsByCategoryAndItemName(categoryId, search, pageRequest);
                items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
            } else {
                items = itemAdminService.searchItemsByName(search, pageRequest);
                items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
            }
        } else {
            if (categoryId != null) {
                items = itemAdminService.getItemsByCategory(categoryId, pageRequest);
                items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
            } else {
                items = itemAdminService.getItems(pageRequest);
                items.forEach(item -> item.setQuantity(inventoryService.getStock(item.getItemId())));
            }
        }
        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        return "admin/items/itemList";
    }

    @PostMapping("/create")
    public String createItem(@Valid @ModelAttribute("item") ItemRequestDto itemRequestDto,
                             List<MultipartFile> itemImgFileList) {
        try {
            itemRequestDto.setItemImgFileList(itemImgFileList);
            CreateItemResponse newItem = itemAdminService.createItem(itemRequestDto);
            inventoryService.setStock(newItem.getItemId(), itemRequestDto.getQuantity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/product/admin/item/list";
    }

    @PutMapping("/update/{itemId}")
    public String updateItem(@PathVariable Long itemId,
                             @Valid @ModelAttribute("item") ItemRequestDto itemRequestDto,
                             List<MultipartFile> itemImgFileList,
                             Long categoryId) {
        try {
            itemAdminService.updateItem(itemId, itemRequestDto, itemImgFileList, categoryId);
            inventoryService.updateStock(itemId, itemRequestDto.getQuantity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/product/admin/item/list";
    }

    @DeleteMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable Long itemId) {
        itemAdminService.deleteItem(itemId);
        inventoryService.deleteStock(itemId);
        return "redirect:/product/admin/item/list";
    }
}
