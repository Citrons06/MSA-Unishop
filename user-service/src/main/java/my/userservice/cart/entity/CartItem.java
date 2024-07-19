package my.userservice.cart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CartItem {

    private Long itemId;
    private int count;
    private String itemName;
    private int price;
}