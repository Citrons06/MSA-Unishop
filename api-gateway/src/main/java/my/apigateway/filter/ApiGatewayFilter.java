package my.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ApiGatewayFilter extends AbstractGatewayFilterFactory<ApiGatewayFilter.Config> {

    private final JwtUtil jwtUtil;

    public static class Config {
    }

    public ApiGatewayFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Custom PRE filter : request id -> {}", request.getId());

            // JWT 토큰 추출
            String token = jwtUtil.resolveToken(request);

            log.info("token: {}", token);
            if (token == null || !jwtUtil.validateToken(token)) {
                // 토큰이 없거나 유효하지 않은 경우
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            // 토큰에서 사용자 이름과 역할 추출
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            if (username != null) {
                // 요청에 사용자 이름과 역할을 헤더로 추가
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Name", username)
                        .header("X-User-Role", role)
                        .build();

                // 사용자 역할과 요청 경로를 확인하여 접근 제어
                if (isAccessDenied(request.getURI().getPath(), role)) {
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return response.setComplete();
                }

                // 수정된 요청으로 필터 체인 계속 진행
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }

            // 필터 체인 계속 진행 후 POST 필터 로깅
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("Custom POST filter : response code -> {}", response.getStatusCode());
            }));
        };
    }

    // 접근 제어 로직
    private boolean isAccessDenied(String path, String role) {
        // 관리자 전용 경로 접근 제어
        if (path.startsWith("/api/product/admin") && !"ADMIN".equals(role)) {
            return true; // 관리자가 아닌 경우
        }
        // 회원 전용 경로 접근 제어
        if (path.startsWith("/api/user/mypage") && !"USER".equals(role) && !"ADMIN".equals(role)) {
            return true; // 회원과 관리자가 아닌 경우
        }
        if (path.startsWith("/api/user/cart") && !"USER".equals(role) && !"ADMIN".equals(role)) {
            return true; // 회원과 관리자가 아닌 경우
        }
        if (path.startsWith("/api/order") && !"USER".equals(role) && !"ADMIN".equals(role)) {
            return true; // 회원과 관리자가 아닌 경우
        }
        if (path.startsWith("/api/pay") && !"USER".equals(role) && !"ADMIN".equals(role)) {
            return true; // 회원과 관리자가 아닌 경우
        }
        return false;
    }
}