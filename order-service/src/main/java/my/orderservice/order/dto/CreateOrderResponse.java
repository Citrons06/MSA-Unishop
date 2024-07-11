package my.orderservice.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.orderservice.order.entity.Order;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    private Long orderId;
    private Long memberId;
    private String orderAddress;
    private LocalDateTime orderDate;
    private int orderPrice;
    private String orderStatus;
    private String orderTel;
    private String orderUsername;

    public CreateOrderResponse(Order order) {
        this.orderId = order.getId();
        this.memberId = order.getMemberId();
        this.orderAddress = order.getOrderAddress();
        this.orderDate = order.getOrderDate();
        this.orderPrice = order.getOrderPrice();
        this.orderStatus = order.getOrderStatus().name();
        this.orderTel = order.getOrderTel();
        this.orderUsername = order.getOrderUsername();
    }
}
