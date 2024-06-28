package my.userservice.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginResponseDto {
    private String username;
    private String refreshToken;
    private String accessToken;

    public LoginResponseDto(String username, String refreshToken, String accessToken) {
        this.username = username;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }
}
