package my.productservice.inventory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class InventoryRequestDto {

    private Long itemId;
    private Integer inventoryStockQuantity;

    public InventoryRequestDto(Long itemId, Integer inventoryStockQuantity) {
        this.itemId = itemId;
        this.inventoryStockQuantity = inventoryStockQuantity;
    }
}
