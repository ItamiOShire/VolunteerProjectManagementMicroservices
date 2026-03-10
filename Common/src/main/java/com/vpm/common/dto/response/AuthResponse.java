package com.vpm.common.dto.response;

public class AuthResponse {

    private long userId;

    public AuthResponse(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}