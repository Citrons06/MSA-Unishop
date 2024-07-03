package my.productservice.inventory.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.inventory.dto.InventoryResponseDto;
import my.productservice.inventory.dto.UpdateQuantityRequest;
import my.productservice.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/api/product/inventory")
@RequiredArgsConstructor
public class InventoryApiController {

    private final InventoryService inventoryService;

    // 재고 수량 조회
    @GetMapping("/list")
    public ResponseEntity<?> getInventoryList() {
        return ResponseEntity.ok(inventoryService.getStockList());
    }

    // 특정 상품의 재고 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getInventory(@PathVariable("itemId") Long itemId) {
        return ResponseEntity.ok(new InventoryResponseDto(itemId, inventoryService.getStock(itemId)));
    }

    // 재고 수정
    @PutMapping("/admin/update/{itemId}")
    public ResponseEntity<?> updateInventory(@PathVariable("itemId") Long itemId, @RequestBody UpdateQuantityRequest request) {
        log.info(itemId + "번 상품의 재고가 수정됩니다. quantity: {}", request.getQuantity());
        inventoryService.setStock(itemId, request.getQuantity());
        return ResponseEntity.ok(new InventoryResponseDto(itemId, request.getQuantity()));
    }
}
