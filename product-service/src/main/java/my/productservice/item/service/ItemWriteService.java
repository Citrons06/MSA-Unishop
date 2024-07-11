package my.productservice.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.aop.DistributedLock;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.service.InventoryService;
import my.productservice.item.entity.Item;
import my.productservice.item.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemWriteService {

    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @DistributedLock(key = "'item:' + #itemId", timeout = 5000, retry = 3)
    public boolean updateQuantityAndSellCount(Long itemId, int quantity) {
        try {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));

            boolean success = inventoryService.updateInventory(itemId, quantity);
            if (!success) {
                log.warn("재고 업데이트 실패: 상품 ID {}, 요청 수량 {}", itemId, quantity);
                return false;
            }

            updateItemSellCount(item, quantity);
            itemRepository.save(item);

            log.info("{} 상품의 재고가 업데이트 되었습니다. [변경량: {}개]", item.getItemName(), quantity);
            return true;
        } catch (CommonException e) {
            log.error("상품 재고 및 판매 수량 업데이트 실패: {}", e.getMessage());
            return false;
        }
    }

    private void updateItemSellCount(Item item, int quantity) {
        if (quantity < 0) {
            item.updateItemSellCount(Math.abs(quantity));
        } else {
            item.updateItemSellCount(-Math.abs(quantity));
        }
    }
}