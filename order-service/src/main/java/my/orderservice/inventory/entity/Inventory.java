package my.orderservice.inventory.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.orderservice.inventory.dto.InventoryRequestDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@NoArgsConstructor
@RedisHash("inventory")
public class Inventory {

    @Id
    private Long id;

    @Setter
    private Integer inventoryStockQuantity;

    @Setter
    private Integer inventoryVer;

    public Inventory(InventoryRequestDto inventoryRequestDto) {
        this.id = inventoryRequestDto.getItemId();
        this.inventoryStockQuantity = inventoryRequestDto.getInventoryStockQuantity();
        this.inventoryVer = inventoryRequestDto.getInventoryVer();

    }

    public void updateStockQuantity(int quantity) {
        this.inventoryStockQuantity += quantity;
    }

    public void increaseInventoryVer() {
        this.inventoryVer++;
    }
}
