package my.userservice.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginResponseDto {
    private String username;
    private String accessToken;

    public LoginResponseDto(String username, String accessToken) {
        this.username = username;
        this.accessToken = accessToken;
    }
}
