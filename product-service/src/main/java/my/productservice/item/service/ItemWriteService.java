package my.productservice.item.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.entity.Inventory;
import my.productservice.inventory.repository.InventoryRepository;
import my.productservice.item.dto.ItemResponseDto;
import my.productservice.item.entity.Item;
import my.productservice.item.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemWriteService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    public ItemResponseDto updateQuantityAndSellCount(Long itemId, int quantity) {
        Item item = itemRepository.findItemById(itemId);

        if (item == null) {
            throw new CommonException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (quantity == 0) {
            throw new CommonException(ErrorCode.UPDATE_FAILED);
        }
        Inventory inventory = inventoryRepository.findByItemId(itemId)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));

        int currentStock = inventory.getInventoryStockQuantity();
        if (currentStock + quantity < 0) {
            throw new CommonException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        inventory.setInventoryStockQuantity(currentStock - quantity);
        inventoryRepository.save(inventory);

        item.updateItemSellCount(quantity);
        log.info("{} 상품의 재고가 업데이트 되었습니다. [재고: {}개]", item.getItemName(), inventory.getInventoryStockQuantity());

        return new ItemResponseDto(item, inventory.getInventoryStockQuantity());
    }
}
