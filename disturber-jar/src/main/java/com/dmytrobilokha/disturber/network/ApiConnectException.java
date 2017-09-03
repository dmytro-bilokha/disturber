package com.dmytrobilokha.disturber.network;

/**
 * The exception to be thrown when connection attempt failed
 */
public class ApiConnectException extends Exception {

    public ApiConnectException(String message, Exception ex) {
        super(message, ex);
    }

}
