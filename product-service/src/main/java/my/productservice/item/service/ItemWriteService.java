package my.productservice.item.service;

<<<<<<< HEAD
=======
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.ws.rs.NotFoundException;
>>>>>>> main
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.service.InventoryService;
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
    private final InventoryService inventoryService;

    @CircuitBreaker(name = "ItemServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    public ItemResponseDto updateQuantityAndSellCount(Long itemId, int quantity) {
        Item item = itemRepository.findItemById(itemId);

        if (item == null) {
            throw new CommonException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (quantity == 0) {
            throw new CommonException(ErrorCode.UPDATE_FAILED);
        }
        int currentStock = inventoryService.getStock(itemId);
        if (currentStock + quantity < 0) {
            throw new CommonException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        inventoryService.updateStock(itemId, quantity);
        item.updateItemSellCount(quantity);
        log.info("재고가 업데이트 되었습니다. [재고: {}", inventoryService.getStock(itemId) + "개]");

        return new ItemResponseDto(item, inventoryService.getStock(itemId));
    }
}
