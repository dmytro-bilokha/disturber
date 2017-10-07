package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.SystemMessage;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    private ApiExceptionToSystemMessageConverter exceptionConverter;
    private int triggerCount;

    @Before
    public void init() throws Exception {
        accountConfig = MockAccountConfigFactory.createMockAccountConfig();
        triggerCount = 0;
        eventQueue = new CrossThreadEventQueue(() -> { triggerCount++; });
        apiConnector = Mockito.mock(MatrixApiConnector.class);
        exceptionConverter = new ApiExceptionToSystemMessageConverter(ResourceBundle.getBundle("messages"));
        synchronizer = new MatrixSynchronizer(accountConfig, eventQueue, apiConnector, exceptionConverter);
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
        orderVerifier.verify(apiConnector).createConnection(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyObject());
        orderVerifier.verify(apiConnector).login(Mockito.anyObject());
        orderVerifier.verify(apiConnector).sync(Mockito.anyObject());
        orderVerifier.verify(apiConnector).sync(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyInt());
    }

    @Test
    public void testCreatesConnection() {
        synchronizer.run();
        Mockito.verify(apiConnector, Mockito.times(1)).createConnection(accountConfig.getServerAddress()
                , accountConfig.getNetworkTimeout(), null);
    }

    @Test
    public void testCreatesConnectionWithProxy() throws Exception {
        AccountConfig account = MockAccountConfigFactory.createMockAccountConfigWithProxy();
        MatrixSynchronizer proxySynchronizer = new MatrixSynchronizer(account, eventQueue, apiConnector, exceptionConverter);
        Mockito.doAnswer(invocation -> {
            proxySynchronizer.disconnect();
            return laterSyncResponse;
        }).when(apiConnector).sync(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyInt());
        proxySynchronizer.run();
        Mockito.verify(apiConnector, Mockito.times(1)).createConnection(account.getServerAddress()
                , account.getNetworkTimeout(), account.getProxyServer());
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

    @Test
    public void testReportsRequestFail() throws ApiConnectException, ApiRequestException {
        Mockito.doAnswer(invocation -> {
            loginPasswordGot = (LoginPasswordDto) invocation.getArguments()[0];
            synchronizer.disconnect();
            throw new ApiRequestException("REQUEST_FAIL", new ApiError(404, "ERROR_CODE", "ERROR_MESSAGE"));
        }).when(apiConnector).login(Mockito.anyObject());
        synchronizer.run();
        Mockito.verify(apiConnector, Mockito.times(1)).login(Mockito.anyObject());
        Mockito.verify(apiConnector, Mockito.times(0)).sync(Mockito.anyObject());
        AppEvent<String, SystemMessage> appEventNewMessage = getEventByType(AppEventType.MATRIX_RESPONSE_FAILED);
        assertNotNull(appEventNewMessage);
        assertTrue(appEventNewMessage.getPayload().getMessage().contains(accountConfig.getUserId()));
        String details = appEventNewMessage.getPayload().getDetails();
        assertTrue(details.contains("REQUEST_FAIL"));
        assertTrue(details.contains("404"));
        assertTrue(details.contains("ERROR_CODE"));
        assertTrue(details.contains("ERROR_MESSAGE"));
    }

    @Test(timeout = 10000)
    public void testHandlesConnectionFail() throws ApiConnectException, ApiRequestException {
        final AtomicInteger counter = new AtomicInteger(0);
        final List<Long> callTimes = new ArrayList<>(5);
        Mockito.doAnswer(invocation -> {
            callTimes.add(System.nanoTime());
            if (counter.incrementAndGet() >= 5)
                synchronizer.disconnect();
            throw new ApiConnectException("NO_CONNECT", new IOException());
        }).when(apiConnector).login(Mockito.anyObject());
        synchronizer.run();
        Mockito.verify(apiConnector, Mockito.times(5)).login(Mockito.anyObject());
        Mockito.verify(apiConnector, Mockito.times(0)).sync(Mockito.anyObject());
        assertTrue(callTimes.size() > 2);
        List<Long> pauseTimes = new ArrayList<>();
        for (int i = 1; i < callTimes.size(); i++) {
            pauseTimes.add((callTimes.get(i) - callTimes.get(i - 1))/1000000);
        }
        System.out.println("For betweenSyncPause=" + accountConfig.getBetweenSyncPause()
                            + " got following on failure pauses: " + pauseTimes);
        for (int i = 1; i < pauseTimes.size(); i++)
            assertTrue(pauseTimes.get(i) > pauseTimes.get(i - 1));
        AppEvent<String, SystemMessage> appEventNewMessage = getEventByType(AppEventType.MATRIX_CONNECTION_FAILED);
        assertNotNull(appEventNewMessage);
        assertTrue(appEventNewMessage.getPayload().getMessage().contains(accountConfig.getUserId()));
        String details = appEventNewMessage.getPayload().getDetails();
        assertTrue(details.contains("NO_CONNECT"));
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
        loginAnswer.setUserId("@LOGIN:my.mockserver.org");
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
