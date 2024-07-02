package my.userservice.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import my.userservice.adapter.OrderDto;
import my.userservice.cart.dto.CartItemResponseDto;
import my.userservice.cart.service.CartService;
import my.userservice.member.dto.*;
import my.userservice.member.service.AuthService;
import my.userservice.member.service.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class MemberApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberServiceImpl memberService;

    @MockBean
    private AuthService authService;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private MemberRequestDto memberRequestDto;
    private LoginRequestDto loginRequestDto;
    private LoginResponseDto loginResponseDto;
    private MemberResponseDto memberResponseDto;
    private List<CartItemResponseDto> cartItems;
    private List<OrderDto> orderDtos;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        memberRequestDto = new MemberRequestDto();
        loginRequestDto = new LoginRequestDto();
        loginResponseDto = new LoginResponseDto();
        memberResponseDto = new MemberResponseDto();
        cartItems = Collections.emptyList();
        orderDtos = Collections.emptyList();
        authResponse = new AuthResponse();
    }

    @Test
    @DisplayName("회원가입 성공")
    public void testSignup() throws Exception {
        // Given
        String requestBody = "{\"username\":null,\"password\":null,\"memberTel\":null,\"memberEmail\":null,\"city\":null,\"street\":null,\"zipcode\":null,\"role\":\"USER\"}";

        // When & Then
        mockMvc.perform(post("/api/user/signup")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.msg").value("인증 메일이 전송되었습니다."));
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void testVerifyEmail() throws Exception {
        when(memberService.verifyEmail(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(get("/api/user/verify-email")
                        .param("token", "token")
                        .param("email", "email@example.com"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/user/join-complete"));
    }

    @Test
    @DisplayName("로그인 성공")
    void testLogin() throws Exception {
        when(authService.login(any(LoginRequestDto.class), any(HttpServletResponse.class))).thenReturn(loginResponseDto);

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void testLogout() throws Exception {
        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "token"))))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 되었습니다."));
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void testGetMember() throws Exception {
        when(cartService.getCart(anyString())).thenReturn(cartItems);
        when(memberService.getOrder(anyString())).thenReturn(orderDtos);

        mockMvc.perform(get("/api/user/mypage")
                        .header("X-User-Name", "username"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cart").isArray())
                .andExpect(jsonPath("$.order").isArray());
    }


    @Test
    @DisplayName("회원 정보 수정 성공")
    void testUpdateMember() throws Exception {
        when(memberService.updateMember(anyString(), any(MemberRequestDto.class))).thenReturn(memberResponseDto);

        mockMvc.perform(patch("/api/user/mypage")
                        .header("X-User-Name", "username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 성공")
    void testRefreshAccessToken() throws Exception {
        // Given
        String refreshToken = "valid_refresh_token";
        String newAccessToken = "new_access_token";
        AuthResponse authResponse = new AuthResponse(newAccessToken);

        given(authService.refreshAccessToken(refreshToken)).willReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/user/refresh")
                        .param("refreshToken", refreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken));
    }

}