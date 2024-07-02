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
        this.itemName = product.getItemName();
        this.count = cartItem.getCount();
        this.price = product.getPrice();
    }

    public CartItemResponseDto(Long itemId, String itemName, int count, int price) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.count = count;
        this.price = price;
    }

    public int getTotalPrice() {
        return price * count;
    }
}
