package com.dmytrobilokha.disturber.appeventbus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class AppEventBusTest {

    private AppEventBus eventBus;
    private long eventCounter;

    @Before
    public void initBus() {
        eventBus = new AppEventBus();
        eventCounter = 0;
    }

    @Test
    public void testPassesClassifiedEventToGeneralSubscriber() {
        AppEventListener<String, Void> mockListener = Mockito.mock(AppEventListener.class);
        eventBus.subscribe(mockListener, AppEventType.MATRIX_LOGGEDIN);
        Mockito.verify(mockListener, Mockito.times(0)).onAppEvent(Mockito.anyObject());
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN"));
        Mockito.verify(mockListener, Mockito.times(1)).onAppEvent(Mockito.anyObject());
        Mockito.verify(mockListener, Mockito.times(1))
                .onAppEvent(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN"));
    }

    @Test
    public void testUnsubscribeWorks() {
        AppEventListener<String, Void> mockListener = Mockito.mock(AppEventListener.class);
        eventBus.subscribe(mockListener, AppEventType.MATRIX_LOGGEDIN);
        Mockito.verify(mockListener, Mockito.times(0)).onAppEvent(Mockito.anyObject());
        eventBus.unsubscribe(mockListener, AppEventType.MATRIX_LOGGEDIN);
        Mockito.verify(mockListener, Mockito.times(0)).onAppEvent(Mockito.anyObject());
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN"));
        Mockito.verify(mockListener, Mockito.times(0)).onAppEvent(Mockito.anyObject());
    }

    @Test
    public void testClassifiesEvents() {
        AppEventListener<String, Void> classifiedMockListener = Mockito.mock(AppEventListener.class);
        AppEventListener<String, Void> generalMockListener = Mockito.mock(AppEventListener.class);
        eventBus.subscribe(classifiedMockListener, AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN42");
        eventBus.subscribe(generalMockListener, AppEventType.MATRIX_LOGGEDIN);
        Mockito.verify(classifiedMockListener, Mockito.times(0)).onAppEvent(Mockito.anyObject());
        Mockito.verify(generalMockListener, Mockito.times(0)).onAppEvent(Mockito.anyObject());
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN"));
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_MOCK"));
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN42"));
        Mockito.verify(classifiedMockListener, Mockito.times(1)).onAppEvent(Mockito.anyObject());
        Mockito.verify(classifiedMockListener, Mockito.times(1))
                .onAppEvent(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN42"));
        Mockito.verify(generalMockListener, Mockito.times(3)).onAppEvent(Mockito.anyObject());
        Mockito.verify(generalMockListener, Mockito.times(1))
                .onAppEvent(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN42"));
    }

    @Test
    public void testIgnoresRepeatedSubscription() {
        AppEventListener<String, Void> mockListener = Mockito.mock(AppEventListener.class);
        eventBus.subscribe(mockListener, AppEventType.MATRIX_LOGGEDIN);
        eventBus.subscribe(mockListener, AppEventType.MATRIX_LOGGEDIN);
        Mockito.verify(mockListener, Mockito.times(0)).onAppEvent(Mockito.anyObject());
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN2"));
        Mockito.verify(mockListener, Mockito.times(1)).onAppEvent(Mockito.anyObject());
    }

    @Test
    public void testWeakReferenceIsWeak() {
        assertEquals(0L, eventCounter);
        subscribeAndCheckEventGot();
        System.gc(); //Trying to force JVM to collect unreferenced listener. Without GC executed the test will fail.
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN2"));
        assertEquals(1L, eventCounter);
    }

    void subscribeAndCheckEventGot() {
        AppEventListener countingListener = createListener();
        eventBus.subscribe(countingListener, AppEventType.MATRIX_LOGGEDIN);
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_LOGGEDIN, "MOCK_LOGIN"));
        assertEquals(1L, eventCounter);
    }

    private AppEventListener createListener() {
        return new AppEventListener() {
            @Override
            public void onAppEvent(AppEvent appEvent) {
                eventCounter++;
            }
        };
    }
}
