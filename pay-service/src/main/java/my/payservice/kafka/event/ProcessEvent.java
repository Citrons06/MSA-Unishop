package my.payservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessEvent {

    private String status;
    private String username;
    private String orderUsername;
    private Long itemId;
    private int quantity;
    private int amount;
    private String orderAddress;
    private String orderTel;
}
