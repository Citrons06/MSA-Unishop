package my.payservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {
    private String status;
    private String username;
    private Long itemId;
    private int quantity;
    private int amount;
    private String payStatus;
}
