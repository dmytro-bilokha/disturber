package com.dmytrobilokha.disturber;

/**
 * The class represents a systemMessage which could be shown to user
 */
public class SystemMessage {

    private final String message;
    private final String details;

    public SystemMessage(String message, String details) {
        this.message = message;
        this.details = details;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
}
