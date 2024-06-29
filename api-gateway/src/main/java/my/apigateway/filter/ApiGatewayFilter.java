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

            String token = jwtUtil.resolveToken(request);

            log.info("token: {}", token);
            if (token == null || !jwtUtil.validateToken(token)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            if (username != null) {
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Name", username)
                        .header("X-User-Role", role)
                        .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("Custom POST filter : response code -> {}", response.getStatusCode());
            }));
        };
    }
}