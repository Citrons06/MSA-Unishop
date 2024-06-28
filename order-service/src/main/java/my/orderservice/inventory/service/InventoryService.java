package my.orderservice.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.inventory.entity.Inventory;
import my.orderservice.inventory.dto.InventoryRequestDto;
import my.orderservice.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService {

    private final RestTemplate restTemplate;
    private final InventoryRepository inventoryRepository;

    private static final String INVENTORY_SERVICE_URL = "http://localhost:8080/api/inventory";

    // 재고 생성
    public void createInventory(Long itemId, int stockQuantity) {
        InventoryRequestDto inventoryDto = new InventoryRequestDto(itemId, stockQuantity, 0); // 초기 버전은 0
        restTemplate.postForObject(INVENTORY_SERVICE_URL, inventoryDto, Void.class);
    }

    // 재고 수정
    public void updateInventory(Long itemId, int updatedStock) {
        Inventory findInventory = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        Integer inventoryVer = findInventory.getInventoryVer();

        InventoryRequestDto inventoryDto = new InventoryRequestDto(itemId, updatedStock, inventoryVer + 1);
        restTemplate.put(INVENTORY_SERVICE_URL + "/" + itemId, inventoryDto);
    }

    // 주문 중 재고 예약
    public void reserveStock(Long itemId, int quantity) {
        Inventory inventory = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        if (inventory.getInventoryStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock");
        }

        inventory.updateStockQuantity(-quantity);  // 재고 감소
        inventory.increaseInventoryVer();

        inventoryRepository.save(inventory);
    }

    // 주문 취소 시 재고 복원
    public void releaseStock(Long itemId, int quantity) {
        Inventory inventory = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        inventory.updateStockQuantity(quantity);
        inventory.increaseInventoryVer();

        inventoryRepository.save(inventory);
    }
}

