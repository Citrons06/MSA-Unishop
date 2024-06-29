package my.userservice.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.adapter.OrderAdapter;
import my.userservice.adapter.OrderDto;
import my.userservice.member.dto.MemberRequestDto;
import my.userservice.member.dto.MemberResponseDto;
import my.userservice.member.entity.Member;
import my.userservice.member.repository.MemberRepository;
import my.userservice.util.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderAdapter orderAdapter;

    @Override
    public void signup(MemberRequestDto memberRequestDto) {
        if (memberRepository.existsByUsername(memberRequestDto.getUsername())) {
            throw new IllegalArgumentException("이미 가입된 회원입니다.");
        }

        if (memberRepository.existsByMemberEmail(memberRequestDto.getMemberEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 암호화 및 DTO 업데이트
        memberRequestDto.setPassword(passwordEncoder.encode(memberRequestDto.getPassword()));

        // 이메일 인증 토큰 생성하고 redis에 저장
        String emailVerificationToken = jwtUtil.generateEmailVerificationToken(memberRequestDto.getMemberEmail());
        redisTemplate.opsForValue().set("emailToken:" + memberRequestDto.getMemberEmail(), emailVerificationToken, 5, TimeUnit.MINUTES);

        // 이메일 전송에 필요한 정보를 redis에 저장
        redisTemplate.opsForValue().set("signupRequest:" + memberRequestDto.getMemberEmail(), memberRequestDto, 15, TimeUnit.MINUTES);

        // 이메일 전송
        emailService.sendMail(memberRequestDto.getMemberEmail(), emailVerificationToken);
    }

    @Override
    public boolean verifyEmail(String email, String token) {
        String storedToken = (String) redisTemplate.opsForValue().get("emailToken:" + email);
        if (storedToken != null && storedToken.equals(token) && jwtUtil.validateToken(token)) {
            // Redis에서 저장된 회원 정보 가져오기
            Object signupRequest = redisTemplate.opsForValue().get("signupRequest:" + email);

            MemberRequestDto memberRequestDto = null;
            if (signupRequest instanceof LinkedHashMap) {
                ObjectMapper objectMapper = new ObjectMapper();
                memberRequestDto = objectMapper.convertValue(signupRequest, MemberRequestDto.class);
            }

            // 회원 정보를 데이터베이스에 저장
            Member member = null;
            if (memberRequestDto != null) {
                member = new Member(memberRequestDto);
                memberRepository.save(member);
            }

            // Redis에서 토큰 및 회원 요청 정보 삭제
            redisTemplate.delete("emailToken:" + email);
            redisTemplate.delete("signupRequest:" + email);

            return true;
        } else {
            return false;
        }
    }

    //회원 정보 수정
    @Override
    public MemberResponseDto updateMember(String username, MemberRequestDto memberRequestDto) {
        Member member = memberRepository.findByUsername(username);
        memberRequestDto.setPassword(passwordEncoder.encode(memberRequestDto.getPassword()));
        member.updateMember(memberRequestDto);

        return new MemberResponseDto(member);
    }

    @Override
    public MemberResponseDto findMember(String username) {
        Member member = memberRepository.findByUsername(username);
        return new MemberResponseDto(member);
    }

    @Override
    public MemberResponseDto getMember(String username) {
        Member member = memberRepository.findByUsername(username);
        return new MemberResponseDto(member);
    }

    @Override
    public List<OrderDto> getOrder(String username) {
        Member member = memberRepository.findByUsername(username);
        return orderAdapter.orderList(member.getUsername());
    }
}