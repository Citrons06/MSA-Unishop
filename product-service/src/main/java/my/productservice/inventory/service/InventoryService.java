package my.productservice.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.dto.InventoryResponseDto;
import my.productservice.inventory.entity.Inventory;
import my.productservice.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public int getStock(Long itemId) {
        Inventory inventory = inventoryRepository.findByItemId(itemId)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));
        return inventory.getInventoryStockQuantity();
    }

    @Transactional
    public void updateStock(Long itemId, int quantity) {
        Inventory inventory = inventoryRepository.findByItemId(itemId)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));
        int newQuantity = inventory.getInventoryStockQuantity() + quantity;
        if (newQuantity < 0) {
            throw new CommonException(ErrorCode.STOCK_NOT_ENOUGH);
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
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));
        inventoryRepository.delete(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getStockList() {
        List<Inventory> inventories = inventoryRepository.findAll();

        return inventories.stream()
                .filter(Objects::nonNull)
                .map(inventory -> new InventoryResponseDto(inventory.getItemId(), inventory.getInventoryStockQuantity()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getStockMap(List<Long> itemIds) {
        List<Inventory> inventories = (List<Inventory>) inventoryRepository.findAllById(itemIds);
        return inventories.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Inventory::getItemId, Inventory::getInventoryStockQuantity));
    }
}
