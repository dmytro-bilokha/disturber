package com.dmytrobilokha.disturber.appeventbus;

import com.dmytrobilokha.disturber.SystemMessage;
import org.junit.Test;

public class AppEventTypeTest {

    @Test
    public void testPayloadForConnectionFailedEventOk() {
        AppEventType.MATRIX_CONNECTION_FAILED.validatePayload(new SystemMessage("blah", "blablah"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNullPayloadForConnectionFailedEvent() {
        AppEventType.MATRIX_CONNECTION_FAILED.validatePayload(null);
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
