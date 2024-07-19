package my.productservice.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.inventory.service.InventoryService;
import my.productservice.item.dto.item.ItemListResponse;
import my.productservice.item.dto.item.ItemResponseDto;
import my.productservice.item.service.ItemReadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

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
        Page<ItemResponseDto> itemsPage = itemReadService.getItems(pageRequest);

        ItemListResponse response = new ItemListResponse();
        response.setItems(itemsPage.getContent().stream()
                .map(this::convertToSimpleItemDto)
                .collect(Collectors.toList()));

        response.setPageInfo(createPageInfo(itemsPage));

        return ResponseEntity.ok().body(response);
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
        Page<ItemResponseDto> itemsPage;
        if (categoryId != null) {
            itemsPage = itemReadService.searchItemsByCategoryAndItemName(categoryId, search, pageRequest);
        } else {
            itemsPage = itemReadService.searchItemsByName(search, pageRequest);
        }

        ItemListResponse response = new ItemListResponse();
        response.setItems(itemsPage.getContent().stream()
                .map(this::convertToSimpleItemDto)
                .collect(Collectors.toList()));
        response.setPageInfo(createPageInfo(itemsPage));

        return ResponseEntity.ok().body(response);
    }

    private ItemListResponse.SimpleItemDto convertToSimpleItemDto(ItemResponseDto item) {
        ItemListResponse.SimpleItemDto simpleItem = new ItemListResponse.SimpleItemDto();
        simpleItem.setItemId(item.getItemId());
        simpleItem.setItemName(item.getItemName());
        simpleItem.setPrice(item.getPrice());
        simpleItem.setQuantity(inventoryService.getStock(item.getItemId()));
        simpleItem.setItemSellStatus(item.getItemSellStatus());
        simpleItem.setPreOrder(item.isPreOrder());

        if (!item.getItemImgList().isEmpty()) {
            simpleItem.setMainImageUrl(item.getItemImgList().get(0).getImgUrl());
        }

        return simpleItem;
    }

    private ItemListResponse.PageInfo createPageInfo(Page<?> page) {
        ItemListResponse.PageInfo pageInfo = new ItemListResponse.PageInfo();
        pageInfo.setCurrentPage(page.getNumber());
        pageInfo.setTotalPages(page.getTotalPages());
        pageInfo.setTotalItems(page.getTotalElements());
        pageInfo.setHasNext(page.hasNext());
        pageInfo.setHasPrevious(page.hasPrevious());
        return pageInfo;
    }
}