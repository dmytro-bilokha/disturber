package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The matrix server response DTO to login attempt
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginAnswerDto {

    @JsonProperty(value = "access_token", required = true)
    private String accessToken;
    @JsonProperty(value = "home_server", required = true)
    private String homeServer;
    @JsonProperty(value = "user_id", required = true)
    private String userId;
    @JsonProperty(value = "refresh_token", required = false)
    private String resfreshToken;
    @JsonProperty(value = "device_id", required = false)
    private String deviceId;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getHomeServer() {
        return homeServer;
    }

    public void setHomeServer(String homeServer) {
        this.homeServer = homeServer;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResfreshToken() {
        return resfreshToken;
    }

    public void setResfreshToken(String resfreshToken) {
        this.resfreshToken = resfreshToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "LoginAnswerDto{" +
                "accessToken='" + accessToken + '\'' +
                ", homeServer='" + homeServer + '\'' +
                ", userId='" + userId + '\'' +
                ", resfreshToken='" + resfreshToken + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
