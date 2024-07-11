package my.productservice.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {

    private final StringRedisTemplate redisTemplate;

    public RedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean lock(String key, String value, long timeout, TimeUnit unit) {
        String script = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value, String.valueOf(unit.toMillis(timeout)));
        return result != null && result == 1;
    }

    public void unlock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        redisTemplate.execute(redisScript, Collections.singletonList(key), value);
    }
}
