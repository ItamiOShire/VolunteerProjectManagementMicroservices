package com.vpm.taskserver.config;

import com.vpm.common.error.ErrorCode;
import com.vpm.common.error.ErrorResponse;
import com.vpm.taskserver.exception.response.GatewayTimeoutException;
import com.vpm.taskserver.exception.response.ServiceTimeoutException;
import com.vpm.taskserver.exception.task.VolunteerNotAssignedToProjectException;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    private final Map<ErrorCode, Function<String, RuntimeException>> exceptions = new HashMap<>(Map.of(
            ErrorCode.ALREADY_ASSIGNED, (message) -> new VolunteerNotAssignedToProjectException(message, ErrorCode.BAD_REQUEST)
    ));

    private final Map<Integer, Function<String, RuntimeException>> feignExceptions = new HashMap<>(Map.of(
            503, GatewayTimeoutException::new,
            504, ServiceTimeoutException::new
    ));

    /*
     * if feign RetryableException is thrown, error decoder is bypassed entirely
     */

    @Override
    public Exception decode(String methodKey, Response response) {

        log.error("DECODER INVOKED: status={}", response.status());

        try {
            ErrorResponse errorResponse = objectMapper.readValue(
                    response.body().asInputStream(),
                    ErrorResponse.class
            );

            if (exceptions.containsKey(errorResponse.getErrorCode())) {
                return exceptions.get(errorResponse.getErrorCode()).apply(errorResponse.getMessage());
            }

            return feignExceptions.getOrDefault(
                    response.status(),
                    (message) -> new RuntimeException("An unexpected error occurred while processing the response - " + message)
            ).apply(response.reason());

        } catch (IOException e) {
            return new RuntimeException("Failed to decode error response: " + e.getMessage());
        }

    }
}
