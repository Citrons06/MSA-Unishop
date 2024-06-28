package my.productservice.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
