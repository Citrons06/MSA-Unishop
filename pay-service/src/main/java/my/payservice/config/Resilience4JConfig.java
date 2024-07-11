package my.payservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
public class Resilience4JConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {

        // 서킷 브레이커 설정 정의
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // 슬라이딩 윈도우 타입을 호출 횟수 기반으로 설정
                .slidingWindowSize(50) // 슬라이딩 윈도우 크기
                .minimumNumberOfCalls(30) // 최소 호출 수
                .waitDurationInOpenState(Duration.ofSeconds(1)) // 서킷 브레이커가 오픈 상태를 유지하는 시간
                .failureRateThreshold(50) // 실패율 임계값
                .slowCallDurationThreshold(Duration.ofSeconds(5)) // 느린 호출로 간주되는 시간
                .slowCallRateThreshold(50) // 느린 호출 임계값
                .permittedNumberOfCallsInHalfOpenState(20) // 반 개방 상태에서 허용되는 호출 수
                .automaticTransitionFromOpenToHalfOpenEnabled(true) // 자동으로 오픈에서 반 개방 상태로 전환하도록 설정
                .build();

        // 타임 리미터 설정 정의
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10)) // 타임아웃 시간
                .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig) // 타임 리미터 적용
                .circuitBreakerConfig(circuitBreakerConfig) // 서킷 브레이커 설정
                .build()
        );
    }

    // 재시도 설정
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3) // 최대 재시도 횟수
                .waitDuration(Duration.ofMillis(500)) // 재시도 간격
                .retryExceptions(IOException.class, TimeoutException.class) // 재시도 대상 예외
                .build();

        return RetryRegistry.of(retryConfig);
    }
}
