package my.userservice.adapter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto {
    private Long itemId;
    private String itemName;
    private Integer price;
    private String itemSellStatus;
    private Integer quantity;
}
