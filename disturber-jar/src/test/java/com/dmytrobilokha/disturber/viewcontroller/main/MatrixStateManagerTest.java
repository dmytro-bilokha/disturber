package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.mockutil.AppEventBusMocker;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore("Ignore during implementing changes phase") //TODO: change the test
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
    public void testSubscribesToEvents() {
/*        assertEquals();
        Mockito.verify(mockBus, Mockito.times(1)).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_LOGGEDIN));
        Mockito.verify(mockBus, Mockito.times(1)).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_NEW_EVENT_GOT));*/
    }

    @Test
    public void testSubscribesToFailEvents() {
        AppEventBus mockBus = Mockito.mock(AppEventBus.class);
        MatrixStateManager keeper = new MatrixStateManager(mockBus, null);
        Mockito.verify(mockBus, Mockito.times(1)).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_CONNECTION_FAILED));
        Mockito.verify(mockBus, Mockito.times(1)).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_RESPONSE_FAILED));
    }

    @Test
    public void testKeepsHistory() {
        RoomKey roomKey = new RoomKey("MOCK_USER", "MOCK_ROOM");
        MatrixEvent mockEvent = getMockEvent();
/*        newMatrixEventListener
                .onAppEvent(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_NEW_EVENT_GOT, roomKey, mockEvent));*/
        //assertEquals(1, stateManager.getRoomEventsHistory(roomKey).size());
        //assertTrue(mockEvent == stateManager.getRoomEventsHistory(roomKey).get(0));
    }

    @Test
    public void testCleansHistoryOnLogin() {
        RoomKey roomKey = new RoomKey("MOCK_USER", "MOCK_ROOM");
        MatrixEvent mockEvent = getMockEvent();
/*        newMatrixEventListener
                .onAppEvent(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_NEW_EVENT_GOT, roomKey, mockEvent));
        loginListener
                .onAppEvent(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, roomKey.getUserId()));*/
        //assertTrue(stateManager.getRoomEventsHistory(roomKey).isEmpty());
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
