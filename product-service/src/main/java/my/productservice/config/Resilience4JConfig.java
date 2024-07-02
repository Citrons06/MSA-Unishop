package my.productservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
<<<<<<< HEAD
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
=======
import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import io.github.resilience4j.spring6.retry.configure.RetryConfigurationProperties;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Qualifier;
>>>>>>> main
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
=======
import org.springframework.context.annotation.Primary;
>>>>>>> main

import java.time.Duration;

@Configuration
public class Resilience4JConfig {

    // 전역 커스텀 서킷 브레이커 설정을 위한 Bean 정의
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {

        // 서킷 브레이커 설정 정의
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // 슬라이딩 윈도우 타입을 호출 횟수 기반으로 설정
                .slidingWindowSize(20) // 슬라이딩 윈도우 크기
                .minimumNumberOfCalls(20) // 최소 호출 수
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 서킷 브레이커가 오픈 상태를 유지하는 시간
                .failureRateThreshold(25) // 실패율 임계값
                .slowCallDurationThreshold(Duration.ofSeconds(2)) // 느린 호출로 간주되는 시간
                .slowCallRateThreshold(25) // 느린 호출이 전체 호출 중 25% 이상이면 서킷 브레이커가 열리도록 설정
                .permittedNumberOfCallsInHalfOpenState(10) // 반 개방 상태에서 허용되는 호출 수
                .automaticTransitionFromOpenToHalfOpenEnabled(true) // 자동으로 오픈에서 반 개방 상태로 전환하도록 설정
                .build();

        // 타임 리미터 설정 정의
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3)) // 타임아웃 시간
                .build();

        // 서킷 브레이커 및 타임 리미터 설정을 적용
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig) // 타임 리미터 적용
                .circuitBreakerConfig(circuitBreakerConfig) // 서킷 브레이커 설정 적용
                .build()
        );
    }
}

