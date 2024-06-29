package my.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

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
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (token != null && jwtUtil.validateToken(token)) {
                String role = jwtUtil.getRoleFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Role", role)
                        .header("X-User-Name", username)
                        .build();

                log.info("role: {}", role);
                log.info("username: {}", username);

                return chain.filter(exchange.mutate().request(request).build());
            }

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        };
    }
}
