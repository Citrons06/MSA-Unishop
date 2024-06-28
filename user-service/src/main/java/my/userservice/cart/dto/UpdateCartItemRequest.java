package my.userservice.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {
    private Long itemId;
    private String itemName;
    private Integer price;
    private Integer quantity;
}