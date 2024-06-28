package my.userservice.adapter;

import lombok.Data;

@Data
public class OrderDto {
    private Long orderId;
    private Long itemId;
    private String itemName;
    private int price;
    private int count;
    private String orderStatus;
    private int orderPrice;
}
