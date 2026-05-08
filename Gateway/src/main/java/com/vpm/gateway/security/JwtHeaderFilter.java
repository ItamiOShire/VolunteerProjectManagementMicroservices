package com.vpm.gateway.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtHeaderFilter implements GlobalFilter {

    @Override
    @NullMarked
    public Mono<Void> filter (
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(authentication -> {

                    if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
                        return chain.filter(exchange);
                    }

                    Jwt jwt = jwtAuth.getToken();

                    assert jwt != null;
                    String userId = jwt.getClaim("userId");
                    String role = jwt.getClaim("role");

                    ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .headers((httpHeaders -> {

                                // prevent spoofing - user self initialized headers should be removed before setting the correct values from the JWT claims
                                httpHeaders.remove("X-User-Id");
                                httpHeaders.remove("X-User-Role");

                                httpHeaders.set("X-User-Id", userId);
                                httpHeaders.set("X-User-Role", role);
                                }
                            ))
                            .build();
                    return chain.filter(
                            exchange.mutate().request(request).build()
                    );
                })
                .switchIfEmpty(chain.filter(exchange));
        }

}
