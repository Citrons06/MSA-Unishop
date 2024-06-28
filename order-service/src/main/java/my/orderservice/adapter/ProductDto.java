package my.orderservice.adapter;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductDto {

    private Long id;
    private String itemName;
    private int price;
    private int quantity;
    private String itemSellStatus;
}
