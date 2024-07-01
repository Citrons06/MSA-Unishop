package my.orderservice.order.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String status;
    private Long memberId;
    private Long itemId;
    private String itemName;
    private String city;
    private String street;
    private String zipcode;
    private String orderTel;
    private String orderUsername;
    private int quantity;
    private int orderPrice;
}