package my.userservice.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemCartRequest {
    private Long itemId;
    private String itemName;
    private Integer price;
    private Integer quantity;
    private String itemSellStatus;
    private Integer stockQuantity;
}
