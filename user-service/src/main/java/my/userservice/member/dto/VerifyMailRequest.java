package my.userservice.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyMailRequest {

    private String email;
    private String code;

    @Builder
    public VerifyMailRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
}
