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

    public AddItemCartRequest(Long itemId, String itemName, Integer price, Integer quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
