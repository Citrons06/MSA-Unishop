package my.productservice.item.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.item.kafka.event.OrderEvent;
import my.productservice.item.service.ItemWriteService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private final ItemWriteService itemWriteService;

    @KafkaListener(topics = "order-topic", groupId = "product-group")
    public void consume(OrderEvent orderEvent) {
        log.info("Consumed event: {}", orderEvent);

        try {
            if ("STOCK_UPDATED".equals(orderEvent.getStatus())) {
                itemWriteService.updateQuantityAndSellCount(orderEvent.getItemId(), orderEvent.getQuantity());
            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }
    }
}
