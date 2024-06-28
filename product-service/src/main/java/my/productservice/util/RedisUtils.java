package my.productservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public void setData(String key, String value, Long expiredTime) {
        redisTemplate.opsForValue().set(key, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    public String getData(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public <T> T get(Long key, Class<T> classType) {
        return get(key.toString(), classType);
    }

    public <T> T get(String key, Class<T> classType) {
        String redisValue = (String) redisTemplate.opsForValue().get(key);

        if (ObjectUtils.isEmpty(redisValue)) {
            return null;
        } else {
            try {
                return mapper.readValue(redisValue, classType);
            } catch (JsonProcessingException e) {
                log.error("Parsing error", e);
                return null;
            }
        }
    }

    public void put(Long key, Object value) {
        put(key.toString(), value);
    }

    public void put(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, mapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            log.error("Failed to write value to Redis", e);
            throw new RuntimeException("Failed to write value to Redis", e);
        }
    }
}