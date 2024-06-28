package my.userservice.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.userservice.adapter.ProductDto;
import my.userservice.cart.entity.CartItem;

@Getter @Setter
@NoArgsConstructor
public class CartItemResponseDto {

    private Long itemId;
    private int count;
    private String itemName;
    private String itemDescription;
    private int price;

    public CartItemResponseDto(CartItem cartItem, ProductDto product) {
        this.itemId = cartItem.getItemId();
        this.count = cartItem.getCount();
        this.itemName = product.getItemName();
        this.price = product.getPrice();
    }

    public int getTotalPrice() {
        return price * count;
    }
}
