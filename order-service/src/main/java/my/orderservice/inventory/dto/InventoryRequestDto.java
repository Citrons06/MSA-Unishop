package my.orderservice.inventory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class InventoryRequestDto {

    private Long itemId;
    private Integer inventoryStockQuantity;
    private Integer inventoryVer;

    public InventoryRequestDto(Long itemId, Integer inventoryStockQuantity, Integer inventoryVer) {
        this.itemId = itemId;
        this.inventoryStockQuantity = inventoryStockQuantity;
        this.inventoryVer = inventoryVer;
    }
}
