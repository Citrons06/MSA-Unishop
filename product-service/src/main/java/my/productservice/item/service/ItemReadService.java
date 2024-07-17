package my.productservice.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.entity.Inventory;
import my.productservice.inventory.repository.InventoryRepository;
import my.productservice.item.dto.SoldTimeDto;
import my.productservice.item.entity.Item;
import my.productservice.item.repository.ItemRepository;
import my.productservice.item.dto.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemReadService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    public Page<ItemResponseDto> getItems(PageRequest pageRequest) {
        Page<Item> items = itemRepository.findAll(pageRequest);
        return items.map(item -> {
            int quantity = getStock(item.getId());
            return new ItemResponseDto(item, quantity);
        });
    }

    public Page<ItemResponseDto> searchItemsByName(String itemName, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByItemNameContaining(itemName, pageRequest);
        return items.map(item -> {
            int quantity = getStock(item.getId());
            return new ItemResponseDto(item, quantity);
        });
    }

    public Page<ItemResponseDto> getItemsByCategory(Long categoryId, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByCategoryId(categoryId, pageRequest);
        return items.map(item -> {
            int quantity = getStock(item.getId());
            return new ItemResponseDto(item, quantity);
        });
    }

    public Page<ItemResponseDto> searchItemsByCategoryAndItemName(Long categoryId, String itemName, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByCategoryIdAndItemNameContaining(categoryId, itemName, pageRequest);
        return items.map(item -> {
            int quantity = getStock(item.getId());
            return new ItemResponseDto(item, quantity);
        });
    }

    public ItemResponseDto getItem(Long id) {
        Item item = itemRepository.findItemById(id);
        int quantity = getStock(id);
        return new ItemResponseDto(item, quantity);
    }

    private int getStock(Long itemId) {
        return inventoryRepository.findByItemId(itemId)
                .map(Inventory::getInventoryStockQuantity)
                .orElse(0);  // 재고 정보가 없으면 0을 반환
    }

    public SoldTimeDto getSoldTime(Long itemId) {
        Item item = itemRepository.findItemById(itemId);
        return new SoldTimeDto(item.isPreOrder(), item.getPreOrderStartAt());
    }
}