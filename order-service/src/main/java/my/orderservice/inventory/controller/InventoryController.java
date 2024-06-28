package my.orderservice.inventory.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.orderservice.inventory.dto.InventoryRequestDto;
import my.orderservice.inventory.entity.Inventory;
import my.orderservice.inventory.repository.InventoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    @PostMapping
    public ResponseEntity<Void> createInventory(@RequestBody InventoryRequestDto inventoryRequestDto) {
        Inventory inventory = new Inventory(inventoryRequestDto);
        inventoryRepository.save(inventory);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<Void> updateInventory(@PathVariable Long itemId, @RequestBody InventoryRequestDto inventoryRequestDto) {
        Inventory inventory = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        inventory.setInventoryStockQuantity(inventoryRequestDto.getInventoryStockQuantity());
        inventory.setInventoryVer(inventoryRequestDto.getInventoryVer());
        inventoryRepository.save(inventory);

        return ResponseEntity.ok().build();
    }
}
