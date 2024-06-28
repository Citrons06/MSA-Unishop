package my.orderservice.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderRequestDto {

    private Long itemId;
    private String itemName;
    private String city;
    private String street;
    private String zipcode;
    private String orderTel;
    private String order_username;
    private int quantity;
    private int orderPrice;

    public OrderRequestDto(Long itemId, String itemName, int price, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.orderPrice = price;
        this.quantity = quantity;
    }
}
