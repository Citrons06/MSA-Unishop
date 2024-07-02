package my.userservice.member.controller.internal;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.userservice.exception.CommonException;
import my.userservice.exception.ErrorCode;
import my.userservice.member.dto.MemberResponseDto;
import my.userservice.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberInternalApiController {

    private final MemberService memberService;

    @GetMapping("/api/user/internal/{username}")
    public ResponseEntity<?> getMember(@PathVariable("username") String username, HttpServletRequest request) {
        try {
            MemberResponseDto memberResponseDto = memberService.getMember(username);
            return ResponseEntity.ok().body(memberResponseDto);
        } catch (Exception e) {
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
