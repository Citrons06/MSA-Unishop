package my.payservice.pay.controller;

import lombok.RequiredArgsConstructor;
import my.payservice.exception.CommonException;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.service.PayService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
public class PayApiController {

    private final PayService payService;

    @PostMapping("/enter")
    public ResponseEntity<?> enterPayment(@RequestBody PayRequest request, @RequestHeader("X-User-Name") String username) {
        try {
            request.setUsername(username);
            payService.enterPayment(request);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제 화면에 진입하였습니다.\"}");  // 결제 시작
        } catch (CommonException e) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"msg\" : \"결제 화면을 이탈하였습니다.\"}");  // 고객 변심 이탈
        }
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processPayment(@RequestBody PayRequest request, @RequestHeader("X-User-Name") String username) {
        request.setUsername(username);
        // PAY_START 상태의 결제가 없으면 에러 발생
        payService.checkPayStatus(request.getUsername());
        payService.processPayment(request);
        return ResponseEntity.ok().build();  // 결제 완료 혹은 고객 귀책 결제 실패
    }
}