package my.apigateway.config;

import my.apigateway.jwt.JwtUtil;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter customGlobalFilter(JwtUtil jwtUtil) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (token != null && jwtUtil.validateToken(token)) {
                String role = jwtUtil.getRoleFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Role", role)
                        .header("X-User-Name", username)
                        .build();

                return chain.filter(exchange.mutate().request(request).build());
            }

            return chain.filter(exchange);
        };
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/internal/user/mypage", "/user/mypage", "/api/internal/cart/**")
                        .filters(f -> f.filter((exchange, chain) -> {
                            // 사용자 권한 체크
                            String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
                            if (role != null && role.equals("USER")) {
                                return chain.filter(exchange);
                            }
                            // 권한이 없는 경우 403 Forbidden 에러 반환
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }))
                        .uri("http://localhost:8081"))

                .route("order-service", r -> r.path("/api/internal/order/**", "/order/**")
                        .filters(f -> f.filter((exchange, chain) -> {
                            String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
                            if (role != null && role.equals("USER")) {
                                return chain.filter(exchange);
                            }

                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }))
                        .uri("http://localhost:8082"))

                .route("product-service-admin", r -> r.path("/product/admin/item/**", "/api/internal/product/admin/item/**",
                                "/product/admin/category", "/api/internal/product/admin/category")
                        .filters(f -> f.filter((exchange, chain) -> {
                            String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
                            // 관리자 권한 체크
                            if (role != null && role.equals("ADMIN")) {
                                return chain.filter(exchange);
                            }

                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }))
                        .uri("http://localhost:8083"))

                .route("product-service", r -> r.path("/api/internal/product/**")
                        .uri("http://localhost:8083"))
                .build();
    }
}