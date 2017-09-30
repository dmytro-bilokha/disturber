package com.dmytrobilokha.disturber.appeventbus;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;

/**
 * All application event types should be in this enumeration
 */
public enum AppEventType {
    MATRIX_NEW_EVENT_GOT(RoomKey.class, MatrixEvent.class)
    , MATRIX_LOGGEDIN(String.class, null)
    , MATRIX_LOGIN_CONNECTION_FAILED(AccountConfig.class, null)
    , MATRIX_LOGIN_FAILED(AccountConfig.class, null)
    , MATRIX_SYNC_CONNECTION_FAILED(AccountConfig.class, null)
    , MATRIX_SYNC_FAILED(AccountConfig.class, null)
    , MATRIX_SYNC_UNKNOWN_FAIL(AccountConfig.class, null)
    , MATRIX_OUTGOING_MESSAGE(RoomKey.class, String.class)
    , MATRIX_SEND_CONNECTION_FAILED(AccountConfig.class, null)
    , MATRIX_SEND_FAILED(AccountConfig.class, null)
    ;

    private final Class classifierClass;
    private final Class payloadClass;

    AppEventType(Class classifierClass, Class payloadClass) {
        this.classifierClass = classifierClass;
        this.payloadClass = payloadClass;
    }

    void validate(Object classifier, Object payload) {
        validateClassifier(classifier);
        validatePayload(payload);
    }

    void validateClassifier(Object classifier) {
        if (classifier == null && classifierClass != null)
            throw new IllegalArgumentException("For event of type '" + this + "' classifier is mandatory, but got null");
        if (classifier != null && classifierClass == null)
            throw new IllegalArgumentException("Got classifier " + classifier + ", but for event of type '"
                    + this + "' classifier is not supported");
        if (classifier != null && !classifierClass.isAssignableFrom(classifier.getClass()))
            throw new IllegalArgumentException("Got illegal classifier object of class '" + classifier.getClass()
                    +"', but for event of type '" + this + "' expected classifier class is '" + classifierClass + '\'');
    }

    void validatePayload(Object payload) {
        if (payload != null && payloadClass == null)
            throw new IllegalArgumentException("Got payload " + payload + ", but for event of type '"
                    + this + "' payload is not supported");
        if (payload == null && payloadClass != null)
            throw new IllegalArgumentException("For event of type '" + this + "' payload is mandatory, but got null");
        if (payload != null && !payloadClass.isAssignableFrom(payload.getClass()))
            throw new IllegalArgumentException("Got illegal payload object of class '" + payload.getClass()
                    +"', but for event of type '" + this + "' expected payload class is '" + payloadClass + '\'');
    }

}
