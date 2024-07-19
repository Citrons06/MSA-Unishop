package my.productservice.item.dto.item;

import lombok.Data;

@Data
public class ItemInternalResponse {

    private Long itemId;
    private String itemName;
    private Integer price;
    private String itemSellStatus;
    private Integer quantity;

    public ItemInternalResponse(ItemResponseDto itemResponseDto) {
        this.itemId = itemResponseDto.getItemId();
        this.itemName = itemResponseDto.getItemName();
        this.price = itemResponseDto.getPrice();
        this.itemSellStatus = itemResponseDto.getItemSellStatus();
        this.quantity = itemResponseDto.getQuantity();
    }
}
