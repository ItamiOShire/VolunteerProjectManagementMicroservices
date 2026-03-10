package com.vpm.common.dto.response;

public class AuthRegistrationResponse {

    private long userId;

    public AuthRegistrationResponse(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}