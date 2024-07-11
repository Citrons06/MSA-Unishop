package my.userservice.member.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.exception.CommonException;
import my.userservice.exception.ErrorCode;
import my.userservice.member.dto.LoginRequestDto;
import my.userservice.member.dto.LoginResponseDto;
import my.userservice.member.dto.MemberResponseDto;
import my.userservice.member.dto.TestMemberSignup;
import my.userservice.member.entity.Member;
import my.userservice.member.repository.MemberRepository;
import my.userservice.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberTestService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    public void testSignup(TestMemberSignup testMemberSignup) {
        log.info("Attempting to sign up user: {}", testMemberSignup.getUsername());
        Member member = new Member(testMemberSignup.getUsername(), testMemberSignup.getPassword());
        memberRepository.save(member);
        log.info("User signed up successfully: {}", testMemberSignup.getUsername());
    }

    public LoginResponseDto testLogin(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();
        log.info("Attempting to log in user: {}", username);

        try {
            Member member = memberRepository.findByUsername(username);

            if (member == null) {
                log.error("User not found: {}", username);
                throw new CommonException(ErrorCode.NOT_AUTHORIZED);
            }

            String role = member.getRole().name();
            String accessToken = jwtUtil.generateAccessToken(username, role);
            String refreshToken = jwtUtil.generateRefreshToken(username, role);

            // 쿠키 설정
            Cookie accessTokenCookie = new Cookie("AccessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60 * 60); // 1시간

            Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);  // 7일

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            // 헤더 설정
            response.setHeader("Authorization", "Bearer " + accessToken);
            response.setHeader("X-User-Name", username);
            response.setHeader("X-User-Role", role);

            log.info("Login success: {}", username);
            return new LoginResponseDto(username, accessToken);

        } catch (CommonException e) {
            log.error("CommonException during login for user: {}", username, e);
            throw e;
        } catch (Exception e) {
            log.error("Exception during login for user: {}", username, e);
            throw new CommonException(ErrorCode.NOT_AUTHORIZED);
        }
    }

    public TestMemberSignup getMember(String username) {
        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new CommonException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return new TestMemberSignup(member);
    }
}
