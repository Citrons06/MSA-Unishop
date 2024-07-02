package my.userservice.member;

import jakarta.servlet.http.HttpServletResponse;
import my.userservice.exception.CommonException;
import my.userservice.member.dto.LoginRequestDto;
import my.userservice.member.entity.Member;
import my.userservice.member.repository.MemberRepository;
import my.userservice.member.service.AuthService;
import my.userservice.refresh.RefreshTokenRepository;
import my.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("로그인 시도 - 비밀번호 불일치로 실패")
    void login_ShouldThrowCommonException_WhenCredentialsAreInvalid() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("username", "wrongPassword");
        Member member = new Member("username", "password");

        when(memberRepository.findByUsername(anyString())).thenReturn(member);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThrows(CommonException.class, () -> authService.login(loginRequestDto, response));
    }

    @Test
    @DisplayName("리프레시 토큰 갱신 시도 - 유효하지 않은 리프레시 토큰으로 실패")
    void refreshAccessToken_ShouldThrowCommonException_WhenRefreshTokenIsInvalid() {
        String refreshToken = "invalidRefreshToken";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        assertThrows(CommonException.class, () -> authService.refreshAccessToken(refreshToken));
    }

    @Test
    @DisplayName("로그아웃 시도 - 유효하지 않은 리프레시 토큰으로 실패")
    void logout_ShouldThrowCommonException_WhenRefreshTokenIsInvalid() {
        String refreshToken = "invalidRefreshToken";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        assertThrows(CommonException.class, () -> authService.logout(refreshToken));
    }
}
