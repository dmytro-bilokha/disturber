package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The class represents container for events queue. It is accessed from worker threads and from the FX application thread
 */
public final class CrossThreadEventQueue {

    private final Queue<AppEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private final Runnable newEventCallback;

    CrossThreadEventQueue(Runnable newEventCallback) {
        this.newEventCallback = newEventCallback;
    }

    void triggerEventCallback() {
        newEventCallback.run();
    }

    <K, T> void addEvent(AppEvent<K, T> event) {
        eventQueue.add(event);
    }

    void addEvents(Collection<AppEvent> events) {
        eventQueue.addAll(events);
    }

    <K, T> AppEvent<K, T> pollEvent() {
        return eventQueue.poll();
    }

}
