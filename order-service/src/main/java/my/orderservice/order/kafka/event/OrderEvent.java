package my.orderservice.order.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.orderservice.order.dto.OrderRequestDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long itemId;
    private Long memberId;
    private int quantity;
    private String status;
    private String username;
    private OrderRequestDto orderRequestDto;
    private String itemName;
}