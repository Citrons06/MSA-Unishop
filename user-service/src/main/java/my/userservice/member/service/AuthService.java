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
import java.util.Date;
import java.util.List;
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

            response.addCookie(accessTokenCookie);

            response.setHeader("Authorization", "Bearer " + accessToken);
            response.setHeader("X-User-Name", username);
            response.setHeader("X-User-Role", role);

            return new LoginResponseDto(username, accessToken, refreshToken);

        } catch (Exception e) {
            log.error("Error authenticating user", e);
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }
    }

    public void logout(LoginRequestDto loginRequestDto, String accessToken) {
        Member member = memberRepository.findByUsername(loginRequestDto.getUsername());

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new CommonException(ErrorCode.NOT_MATCHED);
        }

        if (!jwtUtil.validateToken(accessToken)) {
            throw new CommonException(ErrorCode.NOT_MATCHED);
        }

        // 기존 액세스 토큰을 블랙리스트에 추가
        BlackList blackList = new BlackList(accessToken, jwtUtil.getExpirationDateFromToken(accessToken));
        redisTemplate.opsForValue().set("blacklist:access:" + accessToken, blackList,
                jwtUtil.getRemainTimeFromToken(accessToken), TimeUnit.MILLISECONDS);
    }

    public void logoutAll(LoginRequestDto loginRequestDto, String accessToken) {
        Member member = memberRepository.findByUsername(loginRequestDto.getUsername());

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new CommonException(ErrorCode.NOT_MATCHED);
        }

        if (!jwtUtil.validateToken(accessToken)) {
            throw new CommonException(ErrorCode.NOT_MATCHED);
        }

        // 기존 액세스 토큰을 블랙리스트에 추가
        BlackList accessBlackList = new BlackList(accessToken, jwtUtil.getExpirationDateFromToken(accessToken));
        redisTemplate.opsForValue().set("blacklist:access:" + accessToken, accessBlackList,
                jwtUtil.getRemainTimeFromToken(accessToken), TimeUnit.MILLISECONDS);

        // 해당 사용자의 모든 리프레시 토큰 삭제 및 블랙리스트에 추가
        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUsername(member.getUsername());
        for (RefreshToken refreshToken : refreshTokens) {
            BlackList refreshBlackList = new BlackList(refreshToken.getToken(), LocalDateTime.now().plusDays(7));
            redisTemplate.opsForValue().set("blacklist:refresh:" + refreshToken.getToken(), refreshBlackList, 7, TimeUnit.DAYS);
        }
        refreshTokenRepository.deleteAllByUsername(member.getUsername());
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
