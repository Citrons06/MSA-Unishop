package my.userservice.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CartItemResponseDto {

    private Long itemId;
    private Integer quantity;
    private Integer price;

    public CartItemResponseDto(Long itemId, Integer quantity, Integer price) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.price = price;
    }

    public int getTotalPrice() {
        return price * quantity;
    }
}
