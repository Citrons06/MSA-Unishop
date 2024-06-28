package my.userservice.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CartRequestDto {

    private Integer totalPrice;
    private Integer itemCount;

    public CartRequestDto(Integer totalPrice, Integer itemCount) {
        this.totalPrice = totalPrice;
        this.itemCount = itemCount;
    }
}
