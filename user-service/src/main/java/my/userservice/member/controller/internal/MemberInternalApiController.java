package my.userservice.member.controller.internal;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.userservice.member.dto.MemberResponseDto;
import my.userservice.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/user")
public class MemberInternalApiController {

    private final MemberService memberService;

    @GetMapping("{username}")
    public ResponseEntity<?> getMember(HttpServletRequest request) {
        try {
            String username = request.getHeader("X-User-Name");
            MemberResponseDto memberResponseDto = memberService.getMember(username);
            return ResponseEntity.ok().body(memberResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"회원 정보 조회에 실패하였습니다. 다시 시도해 주세요.\"}");
        }
    }
}
