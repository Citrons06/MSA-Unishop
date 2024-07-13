package my.payservice.pay.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.adapter.UserAdapter;
import my.payservice.adapter.UserDto;
import my.payservice.exception.CommonException;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.dto.ProcessRequest;
import my.payservice.pay.service.PayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
public class PayApiController {

    private final PayService payService;
    private final UserAdapter userAdapter;

    @PostMapping("/enter")
    public ResponseEntity<?> enterPayment(@RequestBody PayRequest request) {
        try {
            return payService.initiatePayment(request);
        } catch (Exception e) {
            log.error("결제 진입 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":500,\"message\":\"" + e.getMessage() + "\",\"data\":null}");
        }
    }

    @PostMapping("/process")
    @CircuitBreaker(name = "payServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
    public ResponseEntity<?> processPayment(@RequestBody ProcessRequest request) {
        // STOCK_DEDUCT 상태의 결제가 없으면 에러 발생
        try {
            UserDto member = userAdapter.getMember(request.getUsername());
            payService.checkPayStatus(member.getUsername());
            request.setUsername(member.getUsername());
            payService.processPayment(request, member.getUsername());

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제를 완료하였습니다.\"}");
        } catch (CommonException e) {
            log.error("Exception caught in controller: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제를 실패하였습니다.\"}");
        }
    }

    public ResponseEntity<?> fallbackMethod(ProcessRequest request, Throwable t) {
        log.error("Fallback method triggered: ", t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("{\"msg\" : \"결제 서비스가 현재 이용 불가능합니다. 나중에 다시 시도해 주세요.\"}");
    }
}