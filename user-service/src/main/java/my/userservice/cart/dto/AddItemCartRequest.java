package my.userservice.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddItemCartRequest {
    private Long itemId;
    private String itemName;
    private Integer price;
    private Integer quantity;
    private String itemSellStatus;
    private Integer stockQuantity;

    public AddItemCartRequest(Long itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public AddItemCartRequest(Long itemId, String itemName, Integer price, Integer quantity, String itemSellStatus, Integer stockQuantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
        this.itemSellStatus = itemSellStatus;
        this.stockQuantity = stockQuantity;

    }
}
