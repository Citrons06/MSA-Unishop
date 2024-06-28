package my.orderservice.common.entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Getter
public abstract class BaseRedisEntity {

    @CreatedDate
    @Indexed
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Indexed
    private LocalDateTime updatedDate;

    public void prePersist() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}