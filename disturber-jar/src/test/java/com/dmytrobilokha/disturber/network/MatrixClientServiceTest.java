package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.MockAccountConfigFactory;
import com.dmytrobilokha.disturber.mockutil.AppEventBusMocker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

public class MatrixClientServiceTest {

    private MatrixClientService clientService;
    private MatrixSynchronizerFactory synchronizerFactory;
    private Runnable clientEventCallback;
    private CrossThreadEventQueue queue;
    private final AppEventBusMocker busMocker = new AppEventBusMocker();
    private final List<MatrixSynchronizer> mockSynchronizers = new ArrayList<>();
    private final List<AccountConfig> accountConfigsConnected = new ArrayList<>();

    @Before
    public void init() {
        queue = new CrossThreadEventQueue(() -> {});
        setupSynchronizerFactoryMock();
        busMocker.init();
        clientService = new MatrixClientService(busMocker.getMockBus(), synchronizerFactory);
    }

    @After
    public void cleanup() {
        cleanupSynchronizerFactory();
        busMocker.clear();
    }

    private void setupSynchronizerFactoryMock() {
        synchronizerFactory = Mockito.mock(MatrixSynchronizerFactory.class);
        Mockito.doAnswer(invocation -> {
            accountConfigsConnected.add((AccountConfig) invocation.getArguments()[0]);
            MatrixSynchronizer mockSynchronizer = Mockito.mock(MatrixSynchronizer.class);
            mockSynchronizers.add(mockSynchronizer);
            return mockSynchronizer;
        }).when(synchronizerFactory).createMatrixSynchronizer(anyObject(), anyObject());
        Mockito.doAnswer(invocation -> {
            clientEventCallback = (Runnable) invocation.getArguments()[0];
            return queue;
        }).when(synchronizerFactory).createCrossThreadEventQueue(anyObject());
    }

    private void cleanupSynchronizerFactory() {
        mockSynchronizers.clear();
        accountConfigsConnected.clear();
    }

    @Test
    public void testOnStartSubscribes() {
        busMocker.validateSubscription(AppEventType.MATRIX_OUTGOING_MESSAGE, AppEventType.MATRIX_CMD_CONNECT
                , AppEventType.MATRIX_CMD_RETRY, AppEventType.MATRIX_JOIN);
        busMocker.getSubscribedClassifiers().forEach(classifier -> assertTrue(classifier == null));
        busMocker.getSubscribedListeners().forEach(listener -> assertTrue(listener != null));
    }

    @Test
    public void testOnStartCreatesEventQueue() {
        assertTrue(clientEventCallback != null);
        Mockito.verify(synchronizerFactory, Mockito.times(1)).createCrossThreadEventQueue(clientEventCallback);
    }

    @Test
    public void testConnects() {
        AccountConfig mockConfig = MockAccountConfigFactory.createMockAccountConfig("1");
        AppEventListener connectListener = findSubscriber(AppEventType.MATRIX_CMD_CONNECT);
        connectListener.onAppEvent(AppEvent.withPayload(AppEventType.MATRIX_CMD_CONNECT, mockConfig));
        Mockito.verify(synchronizerFactory, Mockito.times(1)).createMatrixSynchronizer(mockConfig, queue);
        assertEquals(1, accountConfigsConnected.size());
        assertTrue(accountConfigsConnected.get(0) == mockConfig);
        assertEquals(1, mockSynchronizers.size());
        mockSynchronizers.forEach(synchronizer -> Mockito.verify(synchronizer, Mockito.times(1)).start());
    }

    private AppEventListener findSubscriber(AppEventType eventType) {
        List<AppEventListener> subscribersFound = busMocker.findSubscribers(eventType);
        assertEquals("Exactly one subscriber expected for " + eventType, 1, subscribersFound.size());
        return subscribersFound.get(0);
    }

    @Test
    public void testConnectsOnlyOnce() {
        AccountConfig mockConfig = MockAccountConfigFactory.createMockAccountConfig("1");
        AppEventListener connectListener = findSubscriber(AppEventType.MATRIX_CMD_CONNECT);
        connectListener.onAppEvent(AppEvent.withPayload(AppEventType.MATRIX_CMD_CONNECT, mockConfig));
        connectListener.onAppEvent(AppEvent.withPayload(AppEventType.MATRIX_CMD_CONNECT, mockConfig));
        Mockito.verify(synchronizerFactory, Mockito.times(1)).createMatrixSynchronizer(anyObject(), anyObject());
        assertEquals(1, accountConfigsConnected.size());
    }

    @Test
    public void testPassesEvents() {
        AppEvent event1 = AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "U1");
        AppEvent event2 = AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "U2");
        queue.addEvent(event1);
        queue.addEvent(event2);
        clientEventCallback.run();
        assertTrue(queue.pollEvent() == null); //Queue should be empty
        assertEquals(2, busMocker.getEventsFired().size());
        assertEquals(event1, busMocker.getEventsFired().get(0));
        assertEquals(event2, busMocker.getEventsFired().get(1));
    }

    @Test
    public void testSendsOutgoingMessage() {
        AccountConfig mockConfig = MockAccountConfigFactory.createMockAccountConfig("1");
        AppEventListener connectListener = findSubscriber(AppEventType.MATRIX_CMD_CONNECT);
        connectListener.onAppEvent(AppEvent.withPayload(AppEventType.MATRIX_CMD_CONNECT, mockConfig));
        AppEventListener outgoingMessageListener = findSubscriber(AppEventType.MATRIX_OUTGOING_MESSAGE);
        Mockito.verify(mockSynchronizers.get(0), Mockito.times(0)).enqueueOutgoingMessage(anyString(), anyString());
        outgoingMessageListener.onAppEvent(AppEvent.withClassifierAndPayload(
                AppEventType.MATRIX_OUTGOING_MESSAGE
                , new RoomKey(mockConfig.getUserId(), "ROOM")
                , "MESSAGE"));
        Mockito.verify(mockSynchronizers.get(0), Mockito.times(1)).enqueueOutgoingMessage("ROOM", "MESSAGE");
    }

    @Test
    public void testSendsJoinRequest() {
        AccountConfig mockConfig = MockAccountConfigFactory.createMockAccountConfig("1");
        AppEventListener connectListener = findSubscriber(AppEventType.MATRIX_CMD_CONNECT);
        connectListener.onAppEvent(AppEvent.withPayload(AppEventType.MATRIX_CMD_CONNECT, mockConfig));
        AppEventListener joinRequestListener = findSubscriber(AppEventType.MATRIX_JOIN);
        Mockito.verify(mockSynchronizers.get(0), Mockito.times(0)).enqueueJoin(anyString());
        joinRequestListener.onAppEvent(AppEvent.withClassifier(
                AppEventType.MATRIX_JOIN, new RoomKey(mockConfig.getUserId(), "ROOM")));
        Mockito.verify(mockSynchronizers.get(0), Mockito.times(1)).enqueueJoin("ROOM");
    }

    @Test
    public void testSetsRetry() {
        AccountConfig mockConfig = MockAccountConfigFactory.createMockAccountConfig("1");
        AppEventListener connectListener = findSubscriber(AppEventType.MATRIX_CMD_CONNECT);
        connectListener.onAppEvent(AppEvent.withPayload(AppEventType.MATRIX_CMD_CONNECT, mockConfig));
        AppEventListener retryListener = findSubscriber(AppEventType.MATRIX_CMD_RETRY);
        Mockito.verify(mockSynchronizers.get(0), Mockito.times(0)).setRetryOn();
        retryListener.onAppEvent(AppEvent.withPayload(AppEventType.MATRIX_CMD_RETRY, mockConfig));
        Mockito.verify(mockSynchronizers.get(0), Mockito.times(1)).setRetryOn();
    }

}
