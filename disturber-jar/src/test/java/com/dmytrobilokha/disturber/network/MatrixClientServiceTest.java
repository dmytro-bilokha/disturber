package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.MockAccountConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
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
    private AppEventBus mockBus;
    private final List<AppEventListener> subscribedListeners = new ArrayList<>();
    private final List<AppEventType> subscribedEventTypes = new ArrayList<>();
    private final List<Object> subscribedClassifiers = new ArrayList<>();
    private final List<AppEvent> eventsFired = new ArrayList<>();
    private final List<MatrixSynchronizer> mockSynchronizers = new ArrayList<>();
    private final List<AccountConfig> accountConfigsConnected = new ArrayList<>();

    @Before
    public void init() {
        queue = new CrossThreadEventQueue(() -> {});
        setupSynchronizerFactoryMock();
        setupEventBusMock();
        clientService = new MatrixClientService(mockBus, synchronizerFactory);
    }

    @After
    public void cleanup() {
        cleanupSynchronizerFactory();
        cleanupEventBus();
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

    private void setupEventBusMock() {
        mockBus = Mockito.mock(AppEventBus.class);
        Mockito.doAnswer(invocation -> {
            subscribedListeners.add((AppEventListener) invocation.getArguments()[0]);
            subscribedEventTypes.add((AppEventType) invocation.getArguments()[1]);
            subscribedClassifiers.add(invocation.getArguments()[2]);
            return -1;
        }).when(mockBus).subscribe(anyObject(), anyObject(), anyObject());
        Mockito.doAnswer(invocation -> {
            subscribedListeners.add((AppEventListener) invocation.getArguments()[0]);
            subscribedEventTypes.add((AppEventType) invocation.getArguments()[1]);
            subscribedClassifiers.add(null);
            return -1;
        }).when(mockBus).subscribe(anyObject(), anyObject());
        Mockito.doAnswer(invocation -> {
            eventsFired.add((AppEvent) invocation.getArguments()[0]);
            return -1;
        }).when(mockBus).fire(anyObject());
    }

    private void cleanupEventBus() {
        subscribedListeners.clear();
        subscribedClassifiers.clear();
        subscribedEventTypes.clear();
        eventsFired.clear();
    }

    @Test
    public void testOnStartSubscribes() {
        List<AppEventType> eventTypesToSubscribe = Arrays.asList(AppEventType.MATRIX_OUTGOING_MESSAGE
                                                                , AppEventType.MATRIX_CMD_CONNECT
                                                                , AppEventType.MATRIX_CMD_RETRY);
        assertEquals(eventTypesToSubscribe.size(), subscribedEventTypes.size());
        eventTypesToSubscribe.forEach(eventType -> assertTrue(subscribedEventTypes.contains(eventType)));
        subscribedClassifiers.forEach(classifier -> assertTrue(classifier == null));
        subscribedListeners.forEach(listener -> assertTrue(listener != null));
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
        for (int i = 0; i < subscribedEventTypes.size(); i++) {
            AppEventType subscribedEventType = subscribedEventTypes.get(i);
            if (subscribedEventType == eventType)
                return subscribedListeners.get(i);
        }
        return null;
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
        assertEquals(2, eventsFired.size());
        assertEquals(event1, eventsFired.get(0));
        assertEquals(event2, eventsFired.get(1));
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
