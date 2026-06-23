package com.vpm.gateway.security;

import com.vpm.common.error.ErrorCode;
import com.vpm.common.error.ErrorResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


@Component
public class AuthenticationFailureConfig implements ServerAuthenticationEntryPoint  {

    @Override
    @NullMarked
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException e
    ) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.UNAUTHORIZED,
                e.getMessage(),
                exchange.getRequest().getPath().toString()
        );
        String body = """
                {
                    "timestamp": "%s",
                    "errorCode": "%s",
                    "status": "%s",
                    "message": "%s",
                    "path": "%s",
                }
                """.formatted(
                errorResponse.getTimestamp(),
                errorResponse.getErrorCode(),
                errorResponse.getStatus(),
                errorResponse.getMessage(),
                errorResponse.getPath()
        );

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

}
