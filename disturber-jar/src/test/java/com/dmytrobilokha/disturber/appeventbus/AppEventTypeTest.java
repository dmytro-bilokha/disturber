package com.dmytrobilokha.disturber.appeventbus;

import org.junit.Test;

public class AppEventTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsOnNonNullPayloadForConnectionFailedEvent() {
        AppEventType.MATRIX_LOGIN_CONNECTION_FAILED.validatePayload(new Object());
    }

    @Test
    public void testValidatesNullPayloadForConnectionFailedEvent() {
        AppEventType.MATRIX_LOGIN_CONNECTION_FAILED.validatePayload(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsOnNullClassifierForLoggedInEvent() {
        AppEventType.MATRIX_LOGGEDIN.validateClassifier(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsOnWrongTypeClassifierForLoggedInEvent() {
        AppEventType.MATRIX_LOGGEDIN.validateClassifier(new Object());
    }

    @Test
    public void testValidatesOnRightTypeClassifierForLoggedInEvent() {
        AppEventType.MATRIX_LOGGEDIN.validateClassifier("UserId should be here");
    }

}
