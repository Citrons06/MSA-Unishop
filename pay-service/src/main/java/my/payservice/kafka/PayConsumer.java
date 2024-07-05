package my.payservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.kafka.event.PayEvent;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.service.PayService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayConsumer {

    private static final String PAY_TOPIC = "pay-topic";
    private static final String PAY_GROUP_ID = "pay-group";

    private final PayProducer payProducer;
    private final ProductProducer productProducer;
    private final PayService payService;

    @KafkaListener(topics = PAY_TOPIC, groupId = PAY_GROUP_ID)
    public void consume(PayEvent payEvent) {
        log.info("Consumed event: {}", payEvent);

        try {
            switch (payEvent.getStatus()) {
                case "PAY_START":
                    processPayStart(payEvent);
                    break;
                case "PAY_CANCEL":
                case "PAY_FAILED":
                    processPayCancelOrFailed(payEvent);
                    break;
                case "PAY_COMPLETE":
                    processPayComplete(payEvent);
                    break;
                case "STOCK_DEDUCT":
                    processStockDeduct(payEvent);
                    break;
                default:
                    log.error("Invalid event status: {}", payEvent.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }
    }

    @Transactional
    public void processPayStart(PayEvent payEvent) {
        // 결제 진입 처리
        PayRequest payRequest = new PayRequest(payEvent.getUsername(), payEvent.getItemId(), payEvent.getQuantity(), payEvent.getAmount());
        payService.enterPayment(payRequest);
    }

    @Transactional
    public void processPayCancelOrFailed(PayEvent payEvent) {
        PayEvent stockRecover = new PayEvent("STOCK_RECOVER", payEvent.getUsername(), payEvent.getItemId(), payEvent.getQuantity(), payEvent.getAmount(), "STOCK_RECOVER");
        productProducer.sendProductEvent(stockRecover);
    }

    @Transactional
    public void processPayComplete(PayEvent payEvent) {
        PayEvent orderCreate = new PayEvent("ORDER_CREATE", payEvent.getUsername(), payEvent.getItemId(), payEvent.getQuantity(), payEvent.getAmount(), "ORDER_CREATE");
        payProducer.sendPayEvent(orderCreate);
    }

    @Transactional
    public void processStockDeduct(PayEvent payEvent) {
        // 재고 차감 처리
        log.info("Processing stock deduction: {} {} {} {}", payEvent.getUsername(), payEvent.getItemId(), payEvent.getQuantity(), payEvent.getAmount());

    }
}
