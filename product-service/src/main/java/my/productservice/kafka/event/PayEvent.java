package my.productservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayEvent {

    private String status;
    private String username;
    private Long itemId;
    private int quantity;
    private int amount;
    private String payStatus;
}
