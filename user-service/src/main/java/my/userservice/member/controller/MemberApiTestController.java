package my.userservice.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.exception.CommonException;
import my.userservice.exception.ErrorCode;
import my.userservice.member.dto.*;
import my.userservice.member.service.MemberTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequestMapping("/api/user/test")
@RequiredArgsConstructor
public class MemberApiTestController {

    private final MemberTestService memberTestService;

    @PostMapping("/signup")
    public ResponseEntity<?> testSignup(@RequestBody TestMemberSignup testMemberSignup) {
        memberTestService.testSignup(testMemberSignup);
        return ResponseEntity.ok().contentType(APPLICATION_JSON)
                .body("{\"msg\" : \"[test_mode] 회원가입이 완료되었습니다.\"}");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest, HttpServletResponse response) {
        try {
            log.info("Received login request: {}", loginRequest);
            LoginResponseDto loginResponseDto = memberTestService.testLogin(loginRequest, response);
            return ResponseEntity.ok(loginResponseDto);
        } catch (CommonException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", loginRequest.getUsername(), e);
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }
    }
}
