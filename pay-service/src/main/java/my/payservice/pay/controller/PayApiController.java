package my.payservice.pay.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.exception.CommonException;
import my.payservice.exception.ErrorCode;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.dto.ProcessRequest;
import my.payservice.pay.service.PayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
public class PayApiController {

    private final PayService payService;

    @PostMapping("/enter")
    @CircuitBreaker(name = "payServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    public ResponseEntity<?> enterPayment(@RequestBody PayRequest request, @RequestHeader("X-User-Name") String username) {
        try {
            log.info("Enter payment request: {}", request);
            request.setUsername(username);
            payService.checkQuantity(request);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제 화면에 진입하였습니다.\"}");  // 결제 시작
        } catch (CommonException e) {
            log.error("Exception caught in controller: ", e);
            if (e.getErrorCode() == ErrorCode.PAY_CANCEL) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\" : \"결제 화면을 이탈하였습니다.\"}");  // 고객 변심 이탈
            } else if (e.getErrorCode() == ErrorCode.NOT_ENOUGH_STOCK) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\" : \"결제 실패: 재고가 부족합니다.\"}");  // 재고 부족
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"msg\" : \"결제 실패: 알 수 없는 오류가 발생했습니다.\"}");
            }
        }
    }

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody ProcessRequest request, @RequestHeader("X-User-Name") String username) {
        // PAY_START 상태의 결제가 없으면 에러 발생
        try {
            payService.checkPayStatus(username);
            request.setUsername(username);
            payService.processPayment(request, username);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제를 완료하였습니다.\"}");  // 결제 성공
        } catch (CommonException e) {
            log.error("Exception caught in controller: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제를 실패하였습니다.\"}");
        }
    }

    public ResponseEntity<?> fallbackMethod(PayRequest payRequest, String username, Throwable throwable) {
        String errorMessage = "Pay service is currently unavailable. Please try again later.";
        log.error("Fallback method invoked due to: ", throwable);

        // 기본 응답 생성
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("error", errorMessage);
        fallbackResponse.put("details", payRequest);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
    }
}
