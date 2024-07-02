package my.productservice.inventory.service;

import lombok.RequiredArgsConstructor;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.entity.Inventory;
import my.productservice.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public int getStock(Long itemId) {
        Inventory inventory = inventoryRepository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID: " + itemId));
        return inventory.getInventoryStockQuantity();
    }

    @Transactional
    public void updateStock(Long itemId, int quantity) {
        Inventory inventory = inventoryRepository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID: " + itemId));
        int newQuantity = inventory.getInventoryStockQuantity() + quantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Not enough stock for item ID: " + itemId);
        }
        inventory.setInventoryStockQuantity(newQuantity);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void setStock(Long itemId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByItemId(itemId).orElse(null);
        if (inventory == null) {
            inventory = new Inventory();
            inventory.setItemId(itemId);
        }
        inventory.setInventoryStockQuantity(quantity);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void deleteStock(Long itemId) {
        Inventory inventory = inventoryRepository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID: " + itemId));
        inventoryRepository.delete(inventory);
    }
}
