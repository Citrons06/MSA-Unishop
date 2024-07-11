package my.orderservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

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
    private String eventId;
    private long sequenceNumber;
    private Instant timestamp;

    public ProcessEvent(String status, String username, String orderUsername, Long itemId, int quantity, int amount, String orderAddress, String orderTel) {
        this.status = status;
        this.username = username;
        this.orderUsername = orderUsername;
        this.itemId = itemId;
        this.quantity = quantity;
        this.amount = amount;
        this.orderAddress = orderAddress;
        this.orderTel = orderTel;
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }
}

