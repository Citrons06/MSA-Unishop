package my.productservice.item.controller.internal;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.service.InventoryService;
import my.productservice.item.dto.item.ItemInternalResponse;
import my.productservice.item.dto.item.ItemResponseDto;
import my.productservice.item.dto.item.SoldTimeDto;
import my.productservice.item.service.ItemReadService;
import my.productservice.item.service.ItemWriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/product/api/internal")
@RestController
@RequiredArgsConstructor
public class ItemInternalApiController {

    private final ItemReadService itemReadService;
    private final ItemWriteService itemWriteService;
    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> getItem(@RequestParam("itemId") Long itemId) {
        try {
            ItemResponseDto itemResponseDto = itemReadService.getItem(itemId);
            itemResponseDto.setQuantity(inventoryService.getStock(itemId));
            return ResponseEntity.ok(new ItemInternalResponse(itemResponseDto));
        } catch (NotFoundException e) {
            log.error("Item not found with ID: {}", itemId, e);
            throw new CommonException(ErrorCode.PRODUCT_NOT_FOUND);
        } catch (Exception e) {
            log.error("Error fetching item with ID: {}", itemId, e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update-quantity")
    @CircuitBreaker(name = "ItemServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    public ResponseEntity<?> updateQuantity(@RequestParam("itemId") Long itemId, @RequestParam("quantity") int quantity
                                            ) {
        try {
            itemWriteService.updateQuantityAndSellCount(itemId, quantity);
            return ResponseEntity.ok().body("{\"msg\" : \"재고가 업데이트 되었습니다.\"}");
        } catch (IllegalArgumentException e) {
            log.error("Error updating quantity for item with ID: {}", itemId, e);
            throw new CommonException(ErrorCode.UPDATE_FAILED);
        } catch (Exception e) {
            log.error("Error updating quantity for item with ID: {}", itemId, e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sold-time/{itemId}")
    public SoldTimeDto getSoldTime(@PathVariable("itemId") Long itemId) {
        return itemReadService.getSoldTime(itemId);
    }

    private ItemResponseDto fallbackMethod(Long itemId, int quantity, Throwable throwable) {
        log.error("Fallback method triggered due to: ", throwable);
        throw new CommonException(ErrorCode.SERVICE_UNAVAILABLE);
    }
}