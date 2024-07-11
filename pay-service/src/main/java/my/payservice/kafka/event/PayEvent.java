package my.payservice.kafka.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PayEvent {

    private String status;
    private String username;
    private Long itemId;
    private int quantity;
    private int amount;
    private int itemQuantity;
    private String eventId;
    private long sequenceNumber;
    private Instant timestamp;

    public PayEvent(String status, String username, Long itemId, int quantity, int amount, int itemQuantity) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.status = status;
        this.username = username;
        this.itemId = itemId;
        this.quantity = quantity;
        this.amount = amount;
        this.itemQuantity = itemQuantity;
    }
}
