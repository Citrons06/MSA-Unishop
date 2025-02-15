package my.productservice.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.aop.DistributedLock;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.inventory.dto.InventoryResponseDto;
import my.productservice.item.entity.Item;
import my.productservice.item.repository.ItemRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RedisTemplate<String, Integer> redisTemplate;
    private final ItemRepository itemRepository;
    private static final String INVENTORY_KEY_PREFIX = "stock:";

    private static final long STOCK_EXPIRATION = 30; // 30일

    private final RedisScript<Boolean> updateInventoryScript = RedisScript.of(
            "local current = redis.call('get', KEYS[1]) " +
                    "if current and tonumber(current) + tonumber(ARGV[1]) >= 0 then " +
                    "   redis.call('incrby', KEYS[1], ARGV[1]) " +
                    "   redis.call('expire', KEYS[1], ARGV[2]) " +  // TTL 설정 추가
                    "   return true " +
                    "else " +
                    "   return false " +
                    "end",
            Boolean.class
    );

    @Transactional
    //@DistributedLock(key = "'inventory:' + #itemId", timeout = 5000, retry = 3)
    public boolean updateInventory(Long itemId, int quantityChange) {
        String key = INVENTORY_KEY_PREFIX + itemId;
        Boolean result = redisTemplate.execute(updateInventoryScript,
                Collections.singletonList(key),
                String.valueOf(quantityChange),
                String.valueOf(STOCK_EXPIRATION));
        if (Boolean.TRUE.equals(result)) {
            itemRepository.updateItemSellCount(itemId, quantityChange);

            log.info("재고 및 판매량 업데이트 성공: 상품 ID {}, 변경량 {}", itemId, quantityChange);
            return true;
        } else {
            log.warn("재고 업데이트 실패: 상품 ID {}, 변경량 {}", itemId, quantityChange);
            return false;
        }
    }

    @Transactional
    public int getStock(Long itemId) {
        String key = INVENTORY_KEY_PREFIX + itemId;
        Integer stock = redisTemplate.opsForValue().get(key);
        if (stock == null) {
            log.error("재고 정보 없음: 상품 ID {}", itemId);
            return 0;
        }
        // 만료 시간 재설정
        redisTemplate.expire(key, STOCK_EXPIRATION, TimeUnit.DAYS);
        log.info("재고 조회: 상품 ID {}, 수량 {}", itemId, stock);
        return stock;
    }

    @Transactional
    public void setStock(Long itemId, Integer quantity) {
        if (quantity < 0) {
            throw new CommonException(ErrorCode.INVALID_QUANTITY);
        }
        String key = INVENTORY_KEY_PREFIX + itemId;
        redisTemplate.opsForValue().set(key, quantity, Duration.ofDays(STOCK_EXPIRATION));
        log.info("재고 설정: 상품 ID {}, 수량 {}", itemId, quantity);
    }

    @Transactional
    public void updateStock(Long itemId, int quantity) {
        String key = INVENTORY_KEY_PREFIX + itemId;
        Integer currentQuantity = redisTemplate.opsForValue().get(key);
        if (currentQuantity == null) {
            throw new CommonException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        int newQuantity = currentQuantity + quantity;
        if (newQuantity < 0) {
            throw new CommonException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        redisTemplate.opsForValue().set(key, newQuantity, Duration.ofDays(STOCK_EXPIRATION));
        log.info("재고 업데이트: 상품 ID {}, 새 수량 {}", itemId, newQuantity);
    }

    @Transactional
    public void deleteStock(Long itemId) {
        String key = INVENTORY_KEY_PREFIX + itemId;
        Boolean result = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(result)) {
            log.info("재고 정보 삭제: 상품 ID {}", itemId);
        } else {
            log.warn("재고 정보 삭제 실패: 상품 ID {}", itemId);
        }
    }

    @Transactional
    public List<InventoryResponseDto> getStockList() {
        Set<String> keys = redisTemplate.keys(INVENTORY_KEY_PREFIX + "*");
        return Objects.requireNonNull(keys).stream()
                .map(key -> {
                    Long itemId = Long.parseLong(key.substring(INVENTORY_KEY_PREFIX.length()));
                    Integer quantity = redisTemplate.opsForValue().get(key);
                    return new InventoryResponseDto(itemId, quantity);
                })
                .filter(dto -> dto.getInventoryStockQuantity() != null)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getStockMap(List<Long> itemIds) {
        List<String> keys = itemIds.stream()
                .map(id -> INVENTORY_KEY_PREFIX + id)
                .collect(Collectors.toList());
        List<Integer> values = redisTemplate.opsForValue().multiGet(keys);
        Map<Long, Integer> result = new HashMap<>();
        for (int i = 0; i < itemIds.size(); i++) {
            Integer value = values.get(i);
            result.put(itemIds.get(i), value != null ? value : 0);
        }
        return result;
    }
}