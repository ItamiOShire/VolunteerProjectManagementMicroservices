package com.vpm.taskserver.exception.response;

import com.vpm.common.error.ErrorCode;
import com.vpm.taskserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class GatewayTimeoutException extends ApiException {
    public GatewayTimeoutException(String message) {

        super(
                String.format("Gateway timeout: %s", message),
                HttpStatus.GATEWAY_TIMEOUT,
                ErrorCode.GATEWAY_TIMEOUT
        );
    }
}
