package my.payservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useMasterSlaveServers()
                .setMasterAddress("redis://localhost:6380")
                .addSlaveAddress("redis://localhost:6381");
        config.setCodec(new JsonJacksonCodec());
        
        return Redisson.create(config);
    }
}