package my.payservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.master.host}")
    private String masterHost;

    @Value("${spring.data.redis.master.port}")
    private int masterPort;

    @Value("${spring.data.redis.host}")
    private String slaveHost;

    @Value("${spring.data.redis.port}")
    private int slavePort;

    @Bean
    @Primary
    public RedisConnectionFactory redisMasterConnectionFactory() {
        return new LettuceConnectionFactory(masterHost, masterPort);
    }

    @Bean
    public RedisConnectionFactory redisSlaveConnectionFactory() {
        return new LettuceConnectionFactory(slaveHost, slavePort);
    }

    @Bean
    public RedisTemplate<String, String> redisMasterStringTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisMasterConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, Integer> redisMasterTemplate() {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(redisMasterConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, Integer> redisTemplate() {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(redisSlaveConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        return template;
    }
}
