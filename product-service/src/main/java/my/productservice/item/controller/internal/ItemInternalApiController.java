package my.productservice.item.controller.internal;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.item.dto.ItemInternalResponse;
import my.productservice.item.dto.ItemResponseDto;
import my.productservice.item.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ItemInternalApiController {

    private final ItemService itemService;

    @GetMapping("/api/product/internal")
    public ResponseEntity<?> getItem(@RequestParam("itemId") Long itemId) {
        try {
            ItemResponseDto item = itemService.getItem(itemId);
            return ResponseEntity.ok(new ItemInternalResponse(item));
        } catch (NotFoundException e) {
            log.error("Item not found with ID: {}", itemId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
        } catch (Exception e) {
            log.error("Error fetching item with ID: {}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching item");
        }
    }

    @PutMapping("/api/product/internal/update-quantity")
    public ResponseEntity<?> updateQuantity(@RequestParam("itemId") Long itemId, @RequestParam("quantity") int quantity) {
        try {
            ItemResponseDto item = itemService.updateQuantity(itemId, quantity);
            itemService.updateItemSellCount(itemId, item.getQuantity());
            return ResponseEntity.ok(new ItemInternalResponse(item));
        } catch (IllegalArgumentException e) {
            log.error("Error updating quantity for item with ID: {}", itemId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating quantity for item with ID: {}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating quantity");
        }
    }
}