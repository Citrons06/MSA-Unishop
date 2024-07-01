package my.productservice.item.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @CircuitBreaker(name = "ItemServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    public ItemResponseDto updateQuantityAndSellCount(Long itemId, int quantity) {
        Item item = itemRepository.findItemById(itemId);

        if (item == null) {
            throw new NotFoundException("Item not found");
        }
        if (quantity == 0) {
            throw new IllegalArgumentException("Quantity cannot be zero");
        }
        item.updateStock(-quantity);
        item.updateItemSellCount(quantity);
        log.info("재고가 업데이트 되었습니다. [재고: {}", item.getQuantity() + "개]");

        return new ItemResponseDto(item);
    }

}
