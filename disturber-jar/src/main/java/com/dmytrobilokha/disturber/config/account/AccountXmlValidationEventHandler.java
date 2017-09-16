package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.Constants;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

/**
 * The class used during reading account configs from XML file and accumulates all XML validation errors
 */
class AccountXmlValidationEventHandler implements ValidationEventHandler {

    private final StringBuilder errorMessageBuilder = new StringBuilder();
    private boolean errorDetected = false;

    AccountXmlValidationEventHandler() {}

    @Override
    public boolean handleEvent(ValidationEvent event) {
        errorDetected = true;
        errorMessageBuilder
                .append("MESSAGE: ")
                .append(event.getMessage())
                .append(Constants.NEW_LINE)
                .append("EXCEPTION: ")
                .append(event.getLinkedException())
                .append(Constants.NEW_LINE);
        ValidationEventLocator locator = event.getLocator();
        if (locator != null) {
            errorMessageBuilder
                    .append("LINE NUMBER: ")
                    .append(locator.getLineNumber())
                    .append(" COLUMN NUMBER: ")
                    .append(locator.getColumnNumber());
        }
        errorMessageBuilder.append(Constants.NEW_LINE);
        return true;
    }

    boolean isErrorDetected() {
        return errorDetected;
    }

    String getErrorMessage() {
        return errorMessageBuilder.toString();
    }

}
