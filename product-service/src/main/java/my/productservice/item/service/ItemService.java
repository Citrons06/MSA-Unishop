package my.productservice.item.service;

import my.productservice.item.dto.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ItemService {
    Page<ItemResponseDto> getItems(PageRequest pageRequest);

    Page<ItemResponseDto> searchItemsByName(String itemName, PageRequest pageRequest);

    Page<ItemResponseDto> getItemsByCategory(Long categoryId, PageRequest pageRequest);

    Page<ItemResponseDto> searchItemsByCategoryAndItemName(Long categoryId, String itemName, PageRequest pageRequest);

    ItemResponseDto getItem(Long id);

    void updateItemSellCount(Long id, int quantity);

    ItemResponseDto updateQuantity(Long itemId, int quantity);
}
