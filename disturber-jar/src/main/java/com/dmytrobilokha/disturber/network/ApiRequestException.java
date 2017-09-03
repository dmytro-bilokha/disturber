package com.dmytrobilokha.disturber.network;

/**
 * The exception to be used in case of failed request to the API
 */
public class ApiRequestException extends Exception {

    private final ApiError apiError;

    public ApiRequestException(String message, ApiError apiError) {
        super(message);
        this.apiError = apiError;
    }

    public ApiRequestException(String message, Exception ex, ApiError apiError) {
        super(message, ex);
        this.apiError = apiError;
    }

    public ApiRequestException(ApiError apiError) {
        super();
        this.apiError = apiError;
    }

    public ApiError getApiError() {
        return apiError;
    }

}
