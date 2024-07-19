package my.userservice.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.adapter.OrderDto;
import my.userservice.cart.dto.CartItemResponseDto;
import my.userservice.cart.service.CartService;
import my.userservice.exception.CommonException;
import my.userservice.exception.ErrorCode;
import my.userservice.member.dto.*;
import my.userservice.member.service.AuthService;
import my.userservice.member.service.MemberServiceImpl;
import my.userservice.util.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequestMapping("/user/api")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberServiceImpl memberService;
    private final AuthService authService;
    private final CartService cartService;
    private final RedisTemplate<String, String> redisTemplate;

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
                headers.add("Location", "/user-service/user/signup-complete");
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } else {
                throw new CommonException(ErrorCode.NOT_AUTHORIZED);
            }
        } catch (Exception e) {
            log.error("Error verifying email", e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
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
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LoginRequestDto loginRequestDto,
                                    @RequestHeader("Authorization") String authHeader) {
        String accessToken = authHeader.replace("Bearer ", "");
        authService.logout(loginRequestDto, accessToken);
        return ResponseEntity.ok().contentType(APPLICATION_JSON)
                .body("{\"msg\" : \"로그아웃 되었습니다.\"}");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestBody LoginRequestDto loginRequestDto,
                                       @RequestHeader("Authorization") String authHeader) {
        String accessToken = authHeader.replace("Bearer ", "");
        authService.logoutAll(loginRequestDto, accessToken);
        return ResponseEntity.ok().contentType(APPLICATION_JSON)
                .body("{\"msg\" : \"모든 기기에서 로그아웃 되었습니다.\"}");
    }

    @GetMapping("/mypage")
    public ResponseEntity<?> getMember(HttpServletRequest request,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");

            // 토큰이 블랙리스트에 있는지 확인
            if (isTokenBlacklisted(token)) {
                throw new CommonException(ErrorCode.TOKEN_BLACKLISTED);
            }

            String username = request.getHeader("X-User-Name");
            if (username == null) {
                throw new CommonException(ErrorCode.UNAUTHORIZED);
            }

            List<CartItemResponseDto> cart = cartService.getCart(username);
            List<OrderDto> order = memberService.getOrder(username);

            return ResponseEntity.ok().body(Map.of("cart", cart, "order", order));
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getMember", e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/mypage")
    public ResponseEntity<?> updateMember(HttpServletRequest request,
                                          @RequestHeader("Authorization") String authHeader,
                                          @RequestBody MemberRequestDto memberRequestDto) {
        try {
            String token = authHeader.replace("Bearer ", "");

            // 토큰이 블랙리스트에 있는지 확인
            if (isTokenBlacklisted(token)) {
                throw new CommonException(ErrorCode.TOKEN_BLACKLISTED);
            }

            String username = request.getHeader("X-User-Name");
            if (username == null) {
                throw new CommonException(ErrorCode.UNAUTHORIZED);
            }

            MemberResponseDto memberResponseDto = memberService.updateMember(username, memberRequestDto);
            return ResponseEntity.ok().body(memberResponseDto);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating member", e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam("refreshToken") String refreshToken) {
        try {
            AuthResponse authResponse = authService.refreshAccessToken(refreshToken);
            if (authResponse == null || authResponse.getAccessToken() == null) {
                throw new CommonException(ErrorCode.NOT_AUTHORIZED);
            }
            return ResponseEntity.ok().body(Map.of("accessToken", authResponse.getAccessToken()));
        } catch (Exception e) {
            log.error("Error refreshing access token", e);
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }
    }

    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:access:" + token));
    }
}
