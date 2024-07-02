package my.productservice.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.productservice.item.entity.Item;

@Getter @Setter
@NoArgsConstructor
public class CreateItemResponse {

    private Long itemId;
    private String itemName;
    private int price;
    private int quantity;
    private String itemSellStatus;

    public CreateItemResponse(Item item, int quantity) {
        this.itemId = item.getId();
        this.itemName = item.getItemName();
        this.price = item.getPrice();
        this.quantity = quantity;
        this.itemSellStatus = item.getItemSellStatus().name();
    }
}
