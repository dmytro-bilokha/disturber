package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.MockAccountConfigFactory;
import com.dmytrobilokha.disturber.network.dto.EventContentDto;
import com.dmytrobilokha.disturber.network.dto.EventDto;
import com.dmytrobilokha.disturber.network.dto.JoinedRoomDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.RoomsDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import com.dmytrobilokha.disturber.network.dto.TimelineDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MatrixSynchronizerTest {

    //Mock responses
    private LoginAnswerDto loginAnswerDto;
    private SyncResponseDto firstSyncResponse;
    private SyncResponseDto laterSyncResponse;
    //Parameters captured
    private LoginPasswordDto loginPasswordGot;

    //Mocks, tested object, etc
    private MatrixSynchronizer synchronizer;
    private MatrixApiConnector apiConnector;
    private AccountConfig accountConfig;
    private CrossThreadEventQueue eventQueue;
    private int triggerCount;

    @Before
    public void init() throws Exception {
        accountConfig = MockAccountConfigFactory.createMockAccountConfig();
        triggerCount = 0;
        eventQueue = new CrossThreadEventQueue(() -> {
            triggerCount++;
        });
        apiConnector = Mockito.mock(MatrixApiConnector.class);
        synchronizer = new MatrixSynchronizer(accountConfig, eventQueue, apiConnector);
        loginAnswerDto = createMockLoginAnswerDto();
        Mockito.doAnswer(invocation -> {
            loginPasswordGot = (LoginPasswordDto) invocation.getArguments()[0];
            return loginAnswerDto;
        }).when(apiConnector).login(Mockito.anyObject());
        firstSyncResponse = createMockSyncResponseDto("FIRST");
        Mockito.when(apiConnector.sync(Mockito.anyObject())).thenReturn(firstSyncResponse);
        laterSyncResponse = createMockSyncResponseDto("SECOND");
        Mockito.doAnswer(invocation -> {
            synchronizer.disconnect();
            return laterSyncResponse;
        }).when(apiConnector).sync(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyInt());
    }

    @Test
    public void testOrderOfOperations() throws Exception {
        synchronizer.run();
        InOrder orderVerifier = Mockito.inOrder(apiConnector);
        orderVerifier.verify(apiConnector).createConnection(Mockito.anyObject(), Mockito.anyInt());
        orderVerifier.verify(apiConnector).login(Mockito.anyObject());
        orderVerifier.verify(apiConnector).sync(Mockito.anyObject());
        orderVerifier.verify(apiConnector).sync(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyInt());
    }

    @Test
    public void testCreatesConnection() {
        synchronizer.run();
        Mockito.verify(apiConnector, Mockito.times(1)).createConnection(accountConfig.getServerAddress()
                , accountConfig.getNetworkTimeout());
    }

    @Test
    public void testLogins() throws Exception {
        synchronizer.run();
        Mockito.verify(apiConnector, Mockito.times(1)).login(Mockito.anyObject());
        assertNotNull(loginPasswordGot);
        assertEquals(accountConfig.getLogin(), loginPasswordGot.getLogin());
        assertEquals(accountConfig.getPassword(), loginPasswordGot.getPassword());
    }

    @Test
    public void testSyncsFirstTime() throws Exception {
        synchronizer.run();
        Mockito.verify(apiConnector, Mockito.times(1)).sync(loginAnswerDto.getAccessToken());
    }

    @Test
    public void testSyncsNext() throws Exception {
        synchronizer.run();
        Mockito.verify(apiConnector, Mockito.times(1))
                .sync(loginAnswerDto.getAccessToken(), firstSyncResponse.getNextBatch(), accountConfig.getSyncTimeout());
    }

    @Test
    public void testReportsLogin() {
        synchronizer.run();
        assertEquals(1, triggerCount);
        AppEvent<String, Void> appEventLoggedIn = eventQueue.pollEvent();
        assertNotNull(appEventLoggedIn);
        assertEquals(AppEventType.MATRIX_LOGGEDIN, appEventLoggedIn.getType());
        assertEquals(loginAnswerDto.getUserId(), appEventLoggedIn.getClassifier());
    }


    @Test
    public void testPutsMessagesInQueue() {
        fillMockSyncResponse();
        synchronizer.run();
        AppEvent<RoomKey, MatrixEvent> appEventNewMessage = getEventByType(AppEventType.MATRIX_NEW_EVENT_GOT);
        assertNotNull(appEventNewMessage);
        assertEquals(AppEventType.MATRIX_NEW_EVENT_GOT, appEventNewMessage.getType());
        assertEquals(new RoomKey(loginAnswerDto.getUserId(), "THE_ROOM"), appEventNewMessage.getClassifier());
        MatrixEvent expectedMatrixEvent = MatrixEvent.newBuilder()
                .content("THE_BODY")
                .contentType("STR_MSG")
                .sender("SENDER")
                .serverTimestamp(1234567890L)
                .build();
        assertEquals(expectedMatrixEvent, appEventNewMessage.getPayload());
    }

    private void fillMockSyncResponse() {
        EventContentDto eventContentDto = new EventContentDto();
        eventContentDto.setMsgType("STR_MSG");
        eventContentDto.setBody("THE_BODY");
        EventDto eventDto = new EventDto();
        eventDto.setServerTimestamp(1234567890L);
        eventDto.setSender("SENDER");
        eventDto.setContent(eventContentDto);
        TimelineDto timelineDto = new TimelineDto();
        timelineDto.getEvents().add(eventDto);
        JoinedRoomDto joinedRoomDto = new JoinedRoomDto();
        joinedRoomDto.setTimeline(timelineDto);
        firstSyncResponse.getRooms().getJoinedRoomMap().put("THE_ROOM", joinedRoomDto);
    }

    private AppEvent getEventByType(AppEventType type) {
        AppEvent appEvent;
        while ((appEvent = eventQueue.pollEvent()) != null) {
            if (appEvent.getType() == type)
                return appEvent;
        }
        return null;
    }

    private LoginAnswerDto createMockLoginAnswerDto() {
        LoginAnswerDto loginAnswer = new LoginAnswerDto();
        loginAnswer.setUserId("USER_ID");
        loginAnswer.setAccessToken("ASSESS_TOKEN");
        return loginAnswer;
    }

    private SyncResponseDto createMockSyncResponseDto(String nextBatch) {
        SyncResponseDto syncResponse = new SyncResponseDto();
        RoomsDto roomsDto = new RoomsDto();
        syncResponse.setRooms(roomsDto);
        syncResponse.setNextBatch(nextBatch);
        return syncResponse;
    }

}
