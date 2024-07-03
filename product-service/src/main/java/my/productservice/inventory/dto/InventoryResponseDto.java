package my.productservice.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.productservice.inventory.entity.Inventory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDto {

    private Long itemId;
    private Integer inventoryStockQuantity;

    public InventoryResponseDto(Inventory inventory) {
        this.itemId = inventory.getItemId();
        this.inventoryStockQuantity = inventory.getInventoryStockQuantity();
    }
}