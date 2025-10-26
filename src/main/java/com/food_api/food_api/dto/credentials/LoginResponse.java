package com.food_api.food_api.dto.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginResponse {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("token")
    private String token;

    @JsonProperty("message")
    private String message;

    @JsonProperty("userType")
    private String userType;

    @JsonProperty("username")
    private String username;

    public LoginResponse(boolean success, String token, String message, String userType, String username) {
        this.success = success;
        this.token = token;
        this.message = message;
        this.userType = userType;
        this.username = username;
    }

    public static LoginResponse createErrorResponse(String errorMessage) {
        return new LoginResponse(false, null, errorMessage, null, null);
    }
}