package my.productservice.inventory.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash("inventory")
public class Inventory {

    @Id
    private Long id;

    @Indexed @Setter
    private Long itemId;

    @Setter
    private Integer inventoryStockQuantity;

    @Setter
    private Integer inventoryVer;

    public Inventory(Long id, Long itemId, Integer inventoryStockQuantity, Integer inventoryVer) {
        this.id = id;
        this.itemId = itemId;
        this.inventoryStockQuantity = inventoryStockQuantity;
        this.inventoryVer = inventoryVer;
    }

    public Inventory(Long itemId, Integer quantity) {
        this.itemId = itemId;
        this.inventoryStockQuantity = quantity;
    }

    public Inventory() {
        this.itemId = 0L;
        this.inventoryStockQuantity = 0;
    }
}
