package my.userservice.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.adapter.OrderDto;
import my.userservice.cart.dto.CartItemResponseDto;
import my.userservice.cart.service.CartService;
import my.userservice.member.dto.*;
import my.userservice.member.service.AuthService;
import my.userservice.member.service.MemberServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberServiceImpl memberService;
    private final AuthService authService;
    private final CartService cartService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody MemberRequestDto memberRequestDto) {
        memberService.signup(memberRequestDto);
        return ResponseEntity.ok().contentType(APPLICATION_JSON)
                .body("{\"msg\" : \"인증 메일이 전송되었습니다.\"}");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token, @RequestParam("email") String email) {
        try {
            if (memberService.verifyEmail(email, token)) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", "/user/join-complete");
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"만료된 토큰입니다.\"}");
            }
        } catch (Exception e) {
            log.error("Error verifying email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"이메일 인증 중 오류가 발생하였습니다. 다시 시도해 주세요.\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest, HttpServletResponse response) {
        try {
            log.info("Received login request: {}", loginRequest);
            LoginResponseDto loginResponseDto = authService.login(loginRequest, response);
            return ResponseEntity.ok(loginResponseDto);
        } catch (Exception e) {
            log.error("Authentication failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        log.info("Received refresh token: {}", refreshToken);

        try {
            authService.logout(refreshToken);
            return ResponseEntity.ok("로그아웃 되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("Invalid refresh token", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/mypage")
    public ResponseEntity<?> getMember(HttpServletRequest request) {
        try {
            String username = request.getHeader("X-User-Name");
            List<CartItemResponseDto> cart = cartService.getCart(username);
            List<OrderDto> order = memberService.getOrder(username);

            return ResponseEntity.ok().body(Map.of("cart", cart, "order", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"회원 정보 조회에 실패하였습니다. 다시 시도해 주세요.\"}");
        }
    }


    @PatchMapping("/mypage")
    public ResponseEntity<?> updateMember(HttpServletRequest request,
                                          @RequestBody MemberRequestDto memberRequestDto) {
        try {
            String username = request.getHeader("X-User-Name");
            MemberResponseDto memberResponseDto = memberService.updateMember(username, memberRequestDto);
            return ResponseEntity.ok().body(memberResponseDto);
        } catch (Exception e) {
            log.info("Error updating member", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"회원 정보 수정에 실패하였습니다. 다시 시도해 주세요.\"}");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        try {
            AuthResponse authResponse = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok().body(Map.of("accessToken", authResponse.getAccessToken()));
        } catch (Exception e) {
            log.error("Error refreshing access token", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }
}
