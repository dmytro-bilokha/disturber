package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.mockutil.AppEventBusMocker;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

//TODO: refactor MatrixStateManager class to make it more testable
public class MatrixStateManagerTest {

    private final AppEventBusMocker busMocker = new AppEventBusMocker();
    private MatrixStateManager stateManager;

    @Before
    public void init() {
        busMocker.init();
        stateManager = new MatrixStateManager(busMocker.getMockBus(), null);
    }

    @After
    public void cleanup() {
        busMocker.clear();
    }

    @Test
    public void testOnStartSubscribes() {
        busMocker.validateSubscription(
                AppEventType.MATRIX_NEW_EVENT_GOT
                , AppEventType.MATRIX_CONNECTION_FAILED
                , AppEventType.MATRIX_CONNECTION_ISSUE
                , AppEventType.MATRIX_RESPONSE_FAILED
                , AppEventType.MATRIX_JOINED_OK
                , AppEventType.MATRIX_LOGGEDIN
                , AppEventType.MATRIX_SYNCED
                , AppEventType.MATRIX_NEW_INVITE_GOT);
        busMocker.getSubscribedClassifiers().forEach(classifier -> assertTrue(classifier == null));
        busMocker.getSubscribedListeners().forEach(listener -> assertTrue(listener != null));
    }

    private MatrixEvent getMockEvent() {
        return MatrixEvent.newBuilder()
                .content("MOCK_CONTENT")
                .contentType("STRING")
                .sender("SENDER")
                .serverTimestamp(1505326720000L)
                .build();
    }
}
