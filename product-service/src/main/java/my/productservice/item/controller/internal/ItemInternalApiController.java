package my.productservice.item.controller.internal;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.service.InventoryService;
import my.productservice.item.dto.ItemInternalResponse;
import my.productservice.item.dto.ItemResponseDto;
import my.productservice.item.service.ItemReadService;
import my.productservice.item.service.ItemWriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ItemInternalApiController {

    private final ItemReadService itemReadService;
    private final ItemWriteService itemWriteService;
    private final InventoryService inventoryService;

    @GetMapping("/api/product/internal")
    public ResponseEntity<?> getItem(@RequestParam("itemId") Long itemId) {
        try {
            ItemResponseDto item = itemReadService.getItem(itemId);
            int quantity = inventoryService.getStock(itemId);
            return ResponseEntity.ok(new ItemInternalResponse(new ItemResponseDto(item, quantity)));
        } catch (NotFoundException e) {
            log.error("Item not found with ID: {}", itemId, e);
            throw new CommonException(ErrorCode.PRODUCT_NOT_FOUND);
        } catch (Exception e) {
            log.error("Error fetching item with ID: {}", itemId, e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/api/product/internal/update-quantity")
    public ResponseEntity<?> updateQuantity(@RequestParam("itemId") Long itemId, @RequestParam("quantity") int quantity) {
        try {
            ItemResponseDto item = itemWriteService.updateQuantityAndSellCount(itemId, quantity);
            return ResponseEntity.ok(new ItemInternalResponse(item));
        } catch (IllegalArgumentException e) {
            log.error("Error updating quantity for item with ID: {}", itemId, e);
            throw new CommonException(ErrorCode.UPDATE_FAILED);
        } catch (Exception e) {
            log.error("Error updating quantity for item with ID: {}", itemId, e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
