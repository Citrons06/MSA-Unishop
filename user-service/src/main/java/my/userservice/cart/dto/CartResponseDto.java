package my.userservice.cart.dto;

import lombok.Getter;
import lombok.Setter;
import my.userservice.cart.entity.Cart;
import my.userservice.cart.entity.CartItem;

import java.util.List;

@Getter
@Setter
public class CartResponseDto {
    private String memberId;
    private List<CartItem> items;
    private int totalQuantity;
    private int totalPrice;

    public CartResponseDto(String memberId, List<CartItem> items) {
        this.memberId = memberId;
        this.items = items;
        this.totalQuantity = items.stream().mapToInt(CartItem::getCount).sum();
        this.totalPrice = items.stream().mapToInt(item -> item.getPrice() * item.getCount()).sum();
    }

    public CartResponseDto(Cart cart) {
        this.memberId = cart.getMemberId();
        this.items = cart.getItems();
        this.totalQuantity = cart.getItems().stream().mapToInt(CartItem::getCount).sum();
        this.totalPrice = cart.getItems().stream().mapToInt(item -> item.getPrice() * item.getCount()).sum();
    }
}