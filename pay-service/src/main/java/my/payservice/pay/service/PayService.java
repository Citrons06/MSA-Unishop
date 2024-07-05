package my.payservice.pay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.exception.CommonException;
import my.payservice.exception.ErrorCode;
import my.payservice.kafka.PayProducer;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.dto.PayResponse;
import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import my.payservice.kafka.event.PayEvent;
import my.payservice.pay.repository.PayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PayService {

    private final PayProducer payProducer;
    private final PayRepository payRepository;

    // 결제 진입 시뮬레이션
    public void enterPayment(PayRequest payRequest) {
        // 재고 확인 후 재고가 request 수량보다 적으면 실패


        // 재고 차감 이벤트 발행
        PayEvent stockDeduct = new PayEvent("STOCK_DEDUCT", payRequest.getUsername(), payRequest.getItemId(), payRequest.getQuantity(), payRequest.getAmount(), "PAY_START");
        payProducer.sendPayEvent(stockDeduct);

        Pay pay = Pay.builder()
                .amount(payRequest.getAmount())
                .payStatus(PayStatus.PAY_START)
                .username(payRequest.getUsername())
                .build();
        payRepository.save(pay);

        // 20% 확률로 고객 변심에 의한 이탈
        if (Math.random() <= 0.2) {
            // 재고 회복 이벤트 발행
            PayEvent payEvent = new PayEvent("STOCK_RECOVER", payRequest.getUsername(), payRequest.getItemId(), payRequest.getQuantity(), payRequest.getAmount(), "PAY_CANCEL");
            payProducer.sendPayEvent(payEvent);
            pay.setPayStatus(PayStatus.PAY_CANCEL);
            payRepository.save(pay);

            throw new CommonException(ErrorCode.PAY_CANCEL);
        }
    }

    // 결제 완료 시뮬레이션
    public void processPayment(PayRequest payRequest) {
        Pay findPay = payRepository.findByUsernameAndPayStatus(payRequest.getUsername(), PayStatus.PAY_START);

        if (!PayStatus.PAY_START.equals(findPay.getPayStatus())) {
            throw new CommonException(ErrorCode.INVALID_PAY_STATUS);
        }

        // 20% 확률로 고객 귀책에 의한 결제 실패
        if (Math.random() <= 0.2) {
            findPay.setPayStatus(PayStatus.PAY_FAILED);

            // 결제 실패 이벤트 발행
            PayEvent payEvent = new PayEvent("PAY_FAILED", payRequest.getUsername(), payRequest.getItemId(), payRequest.getQuantity(), payRequest.getAmount(), "PAY_CANCEL");
            payProducer.sendPayEvent(payEvent);

            throw new CommonException(ErrorCode.PAY_FAILED);
        }

        findPay.setPayStatus(PayStatus.PAY_COMPLETE);

        // 결제 완료 이벤트 발행
        PayEvent payEvent = new PayEvent("PAY_COMPLETE", payRequest.getUsername(), payRequest.getItemId(), payRequest.getQuantity(), payRequest.getAmount(), "PAY_COMPLETE");
        payProducer.sendPayEvent(payEvent);
    }

    public void checkPayStatus(String username) {
        Pay findPay = payRepository.findByUsernameAndPayStatus(username, PayStatus.PAY_START);

        if (findPay == null) {
            throw new CommonException(ErrorCode.INVALID_PAY_STATUS);
        }
    }
}