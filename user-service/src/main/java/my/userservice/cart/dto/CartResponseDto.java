package my.userservice.cart.dto;

import lombok.Getter;
import lombok.Setter;
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
        this.totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        this.totalPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();
    }
}