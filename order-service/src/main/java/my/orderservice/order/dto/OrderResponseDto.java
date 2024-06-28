package my.orderservice.order.dto;

import lombok.Getter;
import lombok.Setter;
import my.orderservice.order.entity.Order;
import my.orderservice.order.entity.OrderItem;
import my.orderservice.order.entity.OrderStatus;

@Getter
@Setter
public class OrderResponseDto {

    private Long orderId;
    private Long itemId;
    private String itemName;
    private int price;
    private int count;
    private OrderStatus orderStatus;
    private int orderPrice;

    public OrderResponseDto(Order order) {
        this.orderId = order.getId();

        if (!order.getOrderItems().isEmpty()) {
            OrderItem orderItem = order.getOrderItems().get(0);
            this.itemId = orderItem.getItemId();
            this.itemName = orderItem.getItemName();
            this.price = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
            this.orderStatus = order.getOrderStatus();
        }

        this.orderPrice = order.getTotalPrice();
    }
}
