package my.payservice.pay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.exception.CommonException;
import my.payservice.exception.ErrorCode;
import my.payservice.kafka.PayProducer;
import my.payservice.kafka.ProductProducer;
import my.payservice.kafka.event.ProcessEvent;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.dto.ProcessRequest;
import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import my.payservice.kafka.event.PayEvent;
import my.payservice.pay.repository.PayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PayService {

    private final PayProducer payProducer;
    private final PayRepository payRepository;
    private final ProductProducer productProducer;

    public void checkQuantity(PayRequest payRequest) {
        try {
            // 상품 재고 정보 조회 이벤트 발행
            PayEvent getItemEvent = new PayEvent("GET_ITEM", payRequest.getUsername(),
                    payRequest.getItemId(), payRequest.getQuantity(),
                    payRequest.getAmount(), 0);
            productProducer.sendProductEvent(getItemEvent);
        } catch (CommonException e) {
            log.info("Failed to get item information: {}", e.getMessage());
        }
    }

    // 상품 재고 정보 처리
    public void enterPayment(PayEvent payEvent) {
        // 재고 확인 후 재고가 주문 수량보다 적으면 실패 처리
        if (payEvent.getQuantity() > payEvent.getItemQuantity()) {
            throw new CommonException(ErrorCode.NOT_ENOUGH_STOCK);
        } else {
            // 재고 차감 이벤트 발행
            PayEvent stockDeduct = new PayEvent("STOCK_DEDUCT", payEvent.getUsername(),
                    payEvent.getItemId(), payEvent.getQuantity(),
                    payEvent.getAmount(), payEvent.getItemQuantity());
            payProducer.sendPayEvent(stockDeduct);

            Pay pay = Pay.builder()
                    .amount(payEvent.getAmount())
                    .payStatus(PayStatus.PAY_START)
                    .username(payEvent.getUsername())
                    .build();
            payRepository.save(pay);

            // 20% 확률로 고객 변심에 의한 이탈
            if (Math.random() <= 0.2) {
                // 재고 회복 이벤트 발행
                PayEvent payCancelEvent = new PayEvent("PAY_CANCEL", payEvent.getUsername(),
                        payEvent.getItemId(), payEvent.getQuantity(),
                        payEvent.getAmount(), payEvent.getItemQuantity());
                payProducer.sendPayEvent(payCancelEvent);

                throw new CommonException(ErrorCode.PAY_CANCEL);
            }
        }
    }

    // 결제 완료 시뮬레이션
    public void processPayment(ProcessRequest processRequest, String username) {
        Optional<Pay> findPayOpt =
                payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(username, PayStatus.PAY_START);

        if (findPayOpt.isEmpty()) {
            throw new CommonException(ErrorCode.INVALID_PAY_STATUS);
        }

        Pay findPay = findPayOpt.get();

        // 20% 확률로 고객 귀책에 의한 결제 실패
        if (Math.random() <= 0.2) {
            findPay.setPayStatus(PayStatus.PAY_FAILED);

            // 결제 실패 이벤트 발행
            PayEvent payEvent = new PayEvent("PAY_FAILED", processRequest.getOrderUsername(),
                    processRequest.getItemId(), processRequest.getQuantity(),
                    processRequest.getAmount(), 0);
            payProducer.sendPayEvent(payEvent);

            throw new CommonException(ErrorCode.PAY_FAILED);
        }

        findPay.setPayStatus(PayStatus.PAY_COMPLETE);

        // 결제 완료 이벤트 발행
        ProcessEvent processEvent = new ProcessEvent("PAY_COMPLETE", username, processRequest.getOrderUsername(),
                processRequest.getItemId(), processRequest.getQuantity(),
                processRequest.getAmount(),
                processRequest.getOrderAddress(), processRequest.getOrderTel());
        payProducer.sendProcessEvent(processEvent);
    }

    public void checkPayStatus(String username) {
        Optional<Pay> findPayOpt = payRepository.findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(username, PayStatus.PAY_START);

        if (findPayOpt.isEmpty()) {
            throw new CommonException(ErrorCode.INVALID_PAY_STATUS);
        }
    }
}