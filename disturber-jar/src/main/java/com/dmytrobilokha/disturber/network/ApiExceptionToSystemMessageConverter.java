package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.SystemMessage;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Dependent
class ApiExceptionToSystemMessageConverter {

    private static final Map<MatrixSynchronizer.State, String> messageKeyMap;
    static {
        Map<MatrixSynchronizer.State, String> msgKeyMap = new EnumMap<>(MatrixSynchronizer.State.class);
        msgKeyMap.put(MatrixSynchronizer.State.LOGGEDIN, "matrix.login.fail");
        msgKeyMap.put(MatrixSynchronizer.State.SYNCED, "matrix.sync.fail");
        msgKeyMap.put(MatrixSynchronizer.State.MESSAGES_SENT, "matrix.send.fail");
        messageKeyMap = Collections.unmodifiableMap(msgKeyMap);
    }

    private final ResourceBundle messageBundle;

    @Inject
    ApiExceptionToSystemMessageConverter(ResourceBundle messageBundle) {
        this.messageBundle = messageBundle;
    }

    SystemMessage buildSystemMessage(MatrixSynchronizer.State state, String userId, Exception ex) {
        String message = resolveMessage(state, userId);
        String details = buildDetails(ex);
        return new SystemMessage(message, details);
    }

    private String resolveMessage(MatrixSynchronizer.State state, String userId) {
        String messageKey = messageKeyMap.get(state);
        if (messageKey == null)
            return state + " " + userId;
        try {
            String message = messageBundle.getString(messageKey);
            return  MessageFormat.format(message, userId);
        } catch (MissingResourceException ex) {
            return messageKey + " " + userId;
        }
    }

    private String buildDetails(Exception ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        if (ex instanceof ApiRequestException)
            printApiError(printWriter, (ApiRequestException) ex);
        ex.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    private void printApiError(PrintWriter printWriter, ApiRequestException ex) {
        ApiError apiError = ex.getApiError();
        printWriter.println(getMessage("matrix.invalid.response"));
        printWriter.print(getMessage("matrix.network.code"));
        printWriter.println(apiError.getNetworkCode());
        printWriter.print(getMessage("matrix.error.code"));
        printWriter.println(apiError.getErrorCode());
        printWriter.print(getMessage("matrix.error.message"));
        printWriter.println(apiError.getErrorMessage());
    }

    private String getMessage(String key) {
        try {
            return messageBundle.getString(key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }

}
