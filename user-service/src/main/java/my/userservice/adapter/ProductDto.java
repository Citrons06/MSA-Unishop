package my.userservice.adapter;

import lombok.Data;

@Data
public class ProductDto {
    private Long itemId;
    private String itemName;
    private Integer price;
    private String itemSellStatus;
    private Integer quantity;
}
