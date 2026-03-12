package com.vpm.organizationserver.config;

import com.vpm.common.error.ErrorCode;
import com.vpm.common.error.ErrorResponse;
import com.vpm.organizationserver.exception.organization.OrganizationAlreadyExistsException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class OrganizationFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<ErrorCode, Function<String, RuntimeException>> exceptions = new HashMap<>(Map.of(
            ErrorCode.USER_ALREADY_EXISTS, (message) -> new OrganizationAlreadyExistsException(message, ErrorCode.USER_ALREADY_EXISTS)
    ));

    @Override
    public Exception decode(String methodKey, Response response) {

        try {
            ErrorResponse errorResponse = objectMapper.readValue(
                    response.body().asInputStream(),
                    ErrorResponse.class
            );

            if (exceptions.containsKey(errorResponse.getErrorCode())) {
                return exceptions.get(errorResponse.getErrorCode()).apply(errorResponse.getMessage());
            }

        } catch (IOException e) {
            return new RuntimeException("Failed to decode error response: " + e.getMessage());
        }

        return new RuntimeException("An unexpected error occurred while processing the response");

    }
}
