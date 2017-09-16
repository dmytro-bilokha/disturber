package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.MockAccountConfigFactory;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.RoomsDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
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
        eventQueue = new CrossThreadEventQueue(() -> triggerCount++);
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
