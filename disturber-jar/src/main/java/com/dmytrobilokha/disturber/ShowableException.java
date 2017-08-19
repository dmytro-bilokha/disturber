package com.dmytrobilokha.disturber;

/**
 * The class represents exception which contains user-readable systemMessage and intended to be shown to user
 */
public abstract class ShowableException  extends Exception {

    private final SystemMessage systemMessage;

    public ShowableException(SystemMessage systemMessage) {
        super();
        this.systemMessage = systemMessage;
    }

    public ShowableException(SystemMessage systemMessage, Exception exception) {
        super(exception);
        this.systemMessage = systemMessage;
    }

    public SystemMessage getSystemMessage() {
        return systemMessage;
    }

}
