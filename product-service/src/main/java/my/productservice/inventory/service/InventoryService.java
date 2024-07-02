package my.productservice.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String INVENTORY_KEY_PREFIX = "inventory:";

    public int getStock(Long itemId) {
        Integer stock = (Integer) redisTemplate.opsForValue().get(INVENTORY_KEY_PREFIX + itemId);
        return (stock != null) ? stock : 0;
    }

    public void updateStock(Long itemId, int quantity) {
        redisTemplate.opsForValue().increment(INVENTORY_KEY_PREFIX + itemId, quantity);
    }

    public void setStock(Long itemId, int quantity) {
        redisTemplate.opsForValue().set(INVENTORY_KEY_PREFIX + itemId, quantity);
    }
}
