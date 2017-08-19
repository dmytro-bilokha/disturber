package com.dmytrobilokha.disturber;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * The class represents a systemMessage which could be shown to user
 */
public class SystemMessage {

    private final String key;
    private final Object[] parameters;

    public SystemMessage(String key) {
        this.key = key;
        this.parameters = null;
    }

    public SystemMessage(String key, Object... parameters) {
        this.key = key;
        this.parameters = parameters;
    }

    public String getText(ResourceBundle resourceBundle) {
        String message = resourceBundle.getString(key);
        if (parameters == null)
            return message;
        else
            return MessageFormat.format(message, parameters);
    }

}
