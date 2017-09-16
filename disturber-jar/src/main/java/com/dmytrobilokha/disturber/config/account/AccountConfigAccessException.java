package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.ShowableException;
import com.dmytrobilokha.disturber.SystemMessage;

/**
 * The application exception to be used in case of account data read-write issues
 */
public class AccountConfigAccessException extends ShowableException {

    AccountConfigAccessException(SystemMessage systemMessage, Exception ex) {
        super(systemMessage, ex);
    }

    AccountConfigAccessException(SystemMessage systemMessage) {
        super(systemMessage);
    }

}
