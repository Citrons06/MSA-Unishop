package my.productservice.item.dto.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.productservice.item.entity.Item;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemResponse {

    private Long itemId;
    private String itemName;
    private int price;
    private int quantity;
    private String itemSellStatus;
    private boolean isPreOrder;
    private LocalDateTime preOrderStartAt;

    public CreateItemResponse(Item item, int quantity) {
        this.itemId = item.getId();
        this.itemName = item.getItemName();
        this.price = item.getPrice();
        this.quantity = quantity;
        this.itemSellStatus = item.getItemSellStatus().name();
        this.isPreOrder = item.isPreOrder();
        this.preOrderStartAt = item.getPreOrderStartAt();
    }

    public CreateItemResponse(Long itemId, String itemName, int price, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
