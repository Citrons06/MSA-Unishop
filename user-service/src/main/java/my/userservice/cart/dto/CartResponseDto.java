package my.userservice.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.userservice.cart.entity.Cart;
import my.userservice.cart.entity.CartItem;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartResponseDto {
    private String memberId;
    private List<CartItem> items;
    private int totalQuantity;
    private int totalPrice;

    public CartResponseDto(Cart cart) {
        this.memberId = cart.getMemberId();
        this.items = cart.getItems();
        this.totalQuantity = cart.getItems().stream().mapToInt(CartItem::getCount).sum();
        this.totalPrice = cart.getItems().stream().mapToInt(item -> item.getPrice() * item.getCount()).sum();
    }
}