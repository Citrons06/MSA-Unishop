package my.productservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.item.service.ItemWriteService;
import my.productservice.kafka.event.PayEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayConsumer {

    private static final String PAY_TOPIC = "pay-topic";
    private static final String PAY_GROUP_ID = "product-group";

    private final ItemWriteService itemWriteService;

    @KafkaListener(topics = PAY_TOPIC, groupId = PAY_GROUP_ID, containerFactory = "payKafkaListenerContainerFactory")
    public void consume(PayEvent payEvent) {
        log.info("Consumed event: {}", payEvent);

        try {
            switch (payEvent.getStatus()) {
                case "STOCK_DEDUCT":
                    log.info("Updating quantity and sell count");
                    itemWriteService.updateQuantityAndSellCount(payEvent.getItemId(), -payEvent.getQuantity());
                    break;
                case "STOCK_RECOVER":
                    itemWriteService.updateQuantityAndSellCount(payEvent.getItemId(), payEvent.getQuantity());
                    break;
                default:
                    log.error("Invalid event status: {}", payEvent.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }
    }
}
