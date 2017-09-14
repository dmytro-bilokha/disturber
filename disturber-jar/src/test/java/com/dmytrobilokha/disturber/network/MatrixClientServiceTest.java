package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MatrixClientServiceTest {

    private List<AccountConfig> accountConfigsConnected;
    private List<MatrixSynchronizer> mockSynchronizers;
    private MatrixClientService clientService;
    private MatrixSynchronizerFactory synchronizerFactory;
    private Runnable clientEventCallback;
    private CrossThreadEventQueue queue;
    private AppEventBus mockBus;

    @Before
    public void init() {
        queue = new CrossThreadEventQueue(() -> {});
        accountConfigsConnected = new ArrayList<>();
        mockSynchronizers = new ArrayList<>();
        synchronizerFactory = Mockito.mock(MatrixSynchronizerFactory.class);
        Mockito.doAnswer(invocation -> {
            accountConfigsConnected.add((AccountConfig) invocation.getArguments()[0]);
            MatrixSynchronizer mockSynchronizer = Mockito.mock(MatrixSynchronizer.class);
            mockSynchronizers.add(mockSynchronizer);
            return mockSynchronizer;
        }).when(synchronizerFactory).createMatrixSynchronizer(Mockito.anyObject(), Mockito.anyObject());
        Mockito.doAnswer(invocation -> {
            clientEventCallback = (Runnable) invocation.getArguments()[0];
            return queue;
        }).when(synchronizerFactory).createCrossThreadEventQueue(Mockito.anyObject());
        mockBus = Mockito.mock(AppEventBus.class);
        clientService = new MatrixClientService(mockBus, synchronizerFactory);
    }

    @Test
    public void testCreatesEventQueue() {
        assertTrue(clientEventCallback != null);
        Mockito.verify(synchronizerFactory, Mockito.times(1)).createCrossThreadEventQueue(clientEventCallback);
    }

    @Test
    public void testConnects() {
        AccountConfig mockConfig1 = Mockito.mock(AccountConfig.class);
        AccountConfig mockConfig2 = Mockito.mock(AccountConfig.class);
        clientService.connect(Arrays.asList(mockConfig1, mockConfig2));
        Mockito.verify(synchronizerFactory, Mockito.times(2)).createMatrixSynchronizer(Mockito.anyObject(), Mockito.anyObject());
        assertEquals(2, accountConfigsConnected.size());
        assertTrue(accountConfigsConnected.get(0) == mockConfig1);
        assertTrue(accountConfigsConnected.get(1) == mockConfig2);
        assertEquals(2, mockSynchronizers.size());
        mockSynchronizers.forEach(synchronizer -> Mockito.verify(synchronizer, Mockito.times(1)).start());
    }

    @Test
    public void testConnectsOnlyOnce() {
        AccountConfig mockConfig1 = Mockito.mock(AccountConfig.class);
        AccountConfig mockConfig2 = Mockito.mock(AccountConfig.class);
        clientService.connect(Arrays.asList(mockConfig1, mockConfig2));
        clientService.connect(Arrays.asList(mockConfig2, mockConfig1));
        Mockito.verify(synchronizerFactory, Mockito.times(2)).createMatrixSynchronizer(Mockito.anyObject(), Mockito.anyObject());
        assertEquals(2, accountConfigsConnected.size());
    }

    @Test
    public void testPassesEvents() {
        AppEvent event1 = AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "U1");
        AppEvent event2 = AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "U2");
        queue.addEvent(event1);
        queue.addEvent(event2);
        clientEventCallback.run();
        assertTrue(queue.pollEvent() == null); //Queue should be empty
        Mockito.verify(mockBus, Mockito.times(2)).fire(Mockito.anyObject());
        Mockito.verify(mockBus, Mockito.times(1)).fire(event1);
        Mockito.verify(mockBus, Mockito.times(1)).fire(event2);
    }

}
