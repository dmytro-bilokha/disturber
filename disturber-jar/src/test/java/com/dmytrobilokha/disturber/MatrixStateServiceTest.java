package com.dmytrobilokha.disturber;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MatrixStateServiceTest {

    private MatrixStateService eventsKeeper;
    private AppEventListener<RoomKey, MatrixEvent> newMatrixEventListener;
    private AppEventListener<String, Void> loginListener;

    @Before
    public void init() {
        AppEventBus mockBus = Mockito.mock(AppEventBus.class);
        Mockito.doAnswer(invocation ->
                newMatrixEventListener = (AppEventListener<RoomKey, MatrixEvent>) invocation.getArguments()[0])
            .when(mockBus).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_NEW_EVENT_GOT));
        Mockito.doAnswer(invocation ->
                loginListener = (AppEventListener<String, Void>) invocation.getArguments()[0])
                .when(mockBus).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_LOGGEDIN));
        eventsKeeper = new MatrixStateService(mockBus, null, null);

    }

    @Test
    public void testSubscribesToEvents() {
        AppEventBus mockBus = Mockito.mock(AppEventBus.class);
        MatrixStateService keeper = new MatrixStateService(mockBus, null, null);
        Mockito.verify(mockBus, Mockito.times(1)).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_LOGGEDIN));
        Mockito.verify(mockBus, Mockito.times(1)).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_NEW_EVENT_GOT));
    }

    @Test
    public void testSubscribesToFailEvents() {
        AppEventBus mockBus = Mockito.mock(AppEventBus.class);
        MatrixStateService keeper = new MatrixStateService(mockBus, null, null);
        Mockito.verify(mockBus, Mockito.times(1)).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_CONNECTION_FAILED));
        Mockito.verify(mockBus, Mockito.times(1)).subscribe(Mockito.anyObject(), Matchers.eq(AppEventType.MATRIX_RESPONSE_FAILED));
    }

    @Test
    public void testKeepsHistory() {
        RoomKey roomKey = new RoomKey("MOCK_USER", "MOCK_ROOM");
        MatrixEvent mockEvent = getMockEvent();
        newMatrixEventListener
                .onAppEvent(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_NEW_EVENT_GOT, roomKey, mockEvent));
        assertEquals(1, eventsKeeper.getRoomEventsHistory(roomKey).size());
        assertTrue(mockEvent == eventsKeeper.getRoomEventsHistory(roomKey).get(0));
    }

    @Test
    public void testCleansHistoryOnLogin() {
        RoomKey roomKey = new RoomKey("MOCK_USER", "MOCK_ROOM");
        MatrixEvent mockEvent = getMockEvent();
        newMatrixEventListener
                .onAppEvent(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_NEW_EVENT_GOT, roomKey, mockEvent));
        loginListener
                .onAppEvent(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, roomKey.getUserId()));
        assertTrue(eventsKeeper.getRoomEventsHistory(roomKey).isEmpty());
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
