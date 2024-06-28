package my.userservice.cart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CartItem {

    private Long id;
    private String itemName;
    private Integer price;
    private Integer quantity;
}