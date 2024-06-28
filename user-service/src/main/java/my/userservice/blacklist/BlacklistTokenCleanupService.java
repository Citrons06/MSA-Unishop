package my.userservice.blacklist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistTokenCleanupService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void cleanUpExpiredTokens() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(7); // 7일 이전 토큰 삭제
        log.info("오래된 토큰이 삭제됩니다. {}", expiryDate);

        Set<String> keys = redisTemplate.keys("refreshtoken:*");

        if (keys != null && !keys.isEmpty()) {
            List<String> expiredKeys = keys.stream()
                    .filter(key -> {
                        BlackList blackList = (BlackList) redisTemplate.opsForValue().get(key);
                        return blackList != null && blackList.getCreatedDate().isBefore(expiryDate);
                    })
                    .collect(Collectors.toList());

            if (!expiredKeys.isEmpty()) {
                redisTemplate.delete(expiredKeys);
                log.info("Deleted {} expired tokens", expiredKeys.size());
            } else {
                log.info("No expired tokens found");
            }
        }
    }
}
