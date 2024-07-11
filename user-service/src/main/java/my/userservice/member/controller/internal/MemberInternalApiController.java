package my.userservice.member.controller.internal;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.exception.CommonException;
import my.userservice.exception.ErrorCode;
import my.userservice.member.dto.MemberResponseDto;
import my.userservice.member.dto.TestMemberSignup;
import my.userservice.member.service.MemberService;
import my.userservice.member.service.MemberTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberInternalApiController {

    private final MemberService memberService;
    private final MemberTestService memberTestService;

    @GetMapping("/api/user/internal/{username}")
    public ResponseEntity<?> getMember(@PathVariable("username") String username, HttpServletRequest request) {
        try {
            MemberResponseDto memberResponseDto = memberService.getMember(username);
            return ResponseEntity.ok().body(memberResponseDto);
        } catch (CommonException e) {
            log.error("Error getting member: {}", username, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting member: {}", username, e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/user/internal/test/{username}")
    public ResponseEntity<?> getTestMember(@PathVariable("username") String username, HttpServletRequest request) {
        try {
            TestMemberSignup testMember = memberTestService.getMember(username);
            return ResponseEntity.ok().body(testMember);
        } catch (CommonException e) {
            log.error("Error getting member: {}", username, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting member: {}", username, e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
