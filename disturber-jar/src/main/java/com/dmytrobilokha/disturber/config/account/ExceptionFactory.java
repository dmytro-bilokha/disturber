package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.SystemMessage;
import org.xml.sax.SAXException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Dependent
class ExceptionFactory {

    private final ResourceBundle messageBundle;

    @Inject
    ExceptionFactory(ResourceBundle messageBundle) {
        this.messageBundle = messageBundle;
    }

    AccountConfigAccessException failedSchemaCreationOnLoad(String xsdLocation, SAXException ex) {
        String message = getMessage("account.config.load.schema.fail");
        return failedSchemaCreation(message, xsdLocation, ex);
    }

    AccountConfigAccessException failedSchemaCreationOnSave(String xsdLocation, SAXException ex) {
        String message = getMessage("account.config.save.schema.fail");
        return failedSchemaCreation(message, xsdLocation, ex);
    }

    private AccountConfigAccessException failedSchemaCreation(String message, String xsdLocation, SAXException ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println(getFormattedMessage("account.config.schema.fail.details", xsdLocation));
        ex.printStackTrace(printWriter);
        SystemMessage systemMessage = new SystemMessage(message, stringWriter.toString());
        return new AccountConfigAccessException(systemMessage);
    }

    AccountConfigAccessException failedRead(Path filepath, Exception ex) {
        String message = getFormattedMessage("account.config.load.fail", filepath);
        return failedOnIo(message, ex);
    }

    AccountConfigAccessException failedWrite(Path filepath, Exception ex) {
        String message = getFormattedMessage("account.config.save.fail", filepath);
        return failedOnIo(message, ex);
    }

    private AccountConfigAccessException failedOnIo(String message, Exception ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        SystemMessage systemMessage = new SystemMessage(message, stringWriter.toString());
        return new AccountConfigAccessException(systemMessage);
    }

    AccountConfigAccessException failedValidationOnLoad(Path filepath, String errors) {
        return failedValidationOnLoad(filepath, errors, null);
    }

    AccountConfigAccessException failedValidationOnLoad(Path filepath, String errors, JAXBException ex) {
        String message = getFormattedMessage("account.config.load.validation.fail", filepath);
        return failedAccountsValidation(message, errors, ex);
    }

    AccountConfigAccessException failedValidationOnSave(Path filepath, String errors) {
        return failedValidationOnSave(filepath, errors, null);
    }

    AccountConfigAccessException failedValidationOnSave(Path filepath, String errors, JAXBException ex) {
        String message = getFormattedMessage("account.config.save.validation.fail", filepath);
        return failedAccountsValidation(message, errors, ex);
    }

    private AccountConfigAccessException failedAccountsValidation(String message, String errors, JAXBException ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println(getMessage("following.errors"));
        printWriter.println(errors);
        if (ex != null)
            ex.printStackTrace(printWriter);
        SystemMessage systemMessage = new SystemMessage(message, stringWriter.toString());
        return new AccountConfigAccessException(systemMessage);
    }

    private String getMessage(String key) {
        try {
            return messageBundle.getString(key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    private String getFormattedMessage(String key, Object... data) {
        String message;
        try {
            message = messageBundle.getString(key);
        } catch (MissingResourceException ex) {
            return key + " " + Arrays.toString(data);
        }
        return MessageFormat.format(message, data);
    }

}
