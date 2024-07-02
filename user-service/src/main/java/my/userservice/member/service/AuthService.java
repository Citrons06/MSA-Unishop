package my.userservice.member.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.exception.CommonException;
import my.userservice.exception.ErrorCode;
import my.userservice.member.dto.AuthResponse;
import my.userservice.member.dto.LoginRequestDto;
import my.userservice.member.dto.LoginResponseDto;
import my.userservice.member.entity.BlackList;
import my.userservice.member.entity.Member;
import my.userservice.member.repository.MemberRepository;
import my.userservice.refresh.RefreshToken;
import my.userservice.refresh.RefreshTokenRepository;
import my.userservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate;

    public LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        log.info("로그인 시도");
        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();

        try {
            Member member = memberRepository.findByUsername(username);

            if (!passwordEncoder.matches(password, member.getPassword())) {
                throw new CommonException(ErrorCode.NOT_MATCHED);
            }

            String role = member.getRole().name();
            String ext_refreshToken = jwtUtil.generateRefreshToken(username, role);

            // 블랙리스트에 리프레시 토큰이 있는지 확인
            if (isTokenBlacklisted(ext_refreshToken)) {
                throw new CommonException(ErrorCode.NOT_AUTHORIZED);
            }

            String accessToken = jwtUtil.generateAccessToken(username, role);
            String refreshToken = jwtUtil.generateRefreshToken(username, role);

            refreshTokenRepository.save(new RefreshToken(username, refreshToken));

            Cookie accessTokenCookie = new Cookie("AccessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60 * 60 * 1000); // 1시간

            Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60 * 1000);  // 7일

            response.addCookie(accessTokenCookie);

            response.setHeader("Authorization", "Bearer " + accessToken);
            response.setHeader("X-User-Name", username);
            response.setHeader("X-User-Role", role);

            return new LoginResponseDto(username, accessToken);

        } catch (Exception e) {
            log.error("Error authenticating user", e);
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }
    }

    public void logout(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            refreshTokenRepository.deleteByToken(refreshToken);
            // 만료 시간을 7일로 설정
            BlackList blackList = new BlackList(refreshToken, LocalDateTime.now());
            redisTemplate.opsForValue().set("refreshtoken:" + refreshToken, blackList, 7, TimeUnit.DAYS);
        } else {
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }

        Claims claims = jwtUtil.getClaimsFromToken(refreshToken);
        String username = claims.getSubject();

        if (!refreshTokenRepository.existsByToken(refreshToken)) {
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(username, claims.get("role", String.class));
        return new AuthResponse(accessToken);
    }

    private boolean isTokenBlacklisted(String refreshToken) {
        Object blackList = redisTemplate.opsForValue().get("refreshtoken:" + refreshToken);
        return blackList != null;
    }
}
