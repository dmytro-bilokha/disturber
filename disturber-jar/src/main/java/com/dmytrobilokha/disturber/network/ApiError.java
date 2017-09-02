package com.dmytrobilokha.disturber.network;

/**
 * The class represents error in communication with the Matrix server
 */
public class ApiError {

    private final int networkCode;
    private final String errorCode;
    private final String errorMessage;

    public ApiError(int networkCode, String errorCode, String errorMessage) {
        this.networkCode = networkCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getNetworkCode() {
        return networkCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "networkCode=" + networkCode +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}
