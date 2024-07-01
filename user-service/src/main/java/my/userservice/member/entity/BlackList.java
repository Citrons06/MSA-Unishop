package my.userservice.member.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@RedisHash("blacklist")
public class BlackList {

    @Id
    private Long id;

    @Indexed
    private String refreshToken;

    private LocalDateTime createdDate;

    public BlackList(String refreshToken, LocalDateTime createdDate) {
        this.refreshToken = refreshToken;
        this.createdDate = createdDate;
    }
}