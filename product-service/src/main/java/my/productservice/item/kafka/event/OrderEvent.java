package my.productservice.item.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.productservice.item.dto.OrderRequestDto;

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
