package com.dmytrobilokha.disturber.mockutil;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;

public class AppEventBusMocker {

    private AppEventBus mockBus;
    private final List<AppEventListener> subscribedListeners = new ArrayList<>();
    private final List<AppEventType> subscribedEventTypes = new ArrayList<>();
    private final List<Object> subscribedClassifiers = new ArrayList<>();
    private final List<AppEvent> eventsFired = new ArrayList<>();

    public AppEventBusMocker() {
        init();
    }

    public void init() {
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

    public void clear() {
        subscribedListeners.clear();
        subscribedClassifiers.clear();
        subscribedEventTypes.clear();
        eventsFired.clear();
    }

    public AppEventBus getMockBus() {
        return mockBus;
    }

    public List<AppEventListener> getSubscribedListeners() {
        return subscribedListeners;
    }

    public List<AppEventType> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    public List<Object> getSubscribedClassifiers() {
        return subscribedClassifiers;
    }

    public List<AppEvent> getEventsFired() {
        return eventsFired;
    }

    public List<AppEventListener> findSubscribers(AppEventType eventType) {
        List<AppEventListener> subscribersFound = new ArrayList<>();
        for (int i = 0; i < subscribedEventTypes.size(); i++) {
            AppEventType subscribedEventType = subscribedEventTypes.get(i);
            if (subscribedEventType == eventType)
                subscribersFound.add(subscribedListeners.get(i));
        }
        return subscribersFound;
    }

    public void validateSubscription(AppEventType... eventTypes) {
        assertEquals(eventTypes.length, subscribedEventTypes.size());
        for (AppEventType eventType : eventTypes) {
            assertTrue("Expected to find subscription for " + eventType, subscribedEventTypes.contains(eventType));
        }
    }

}
