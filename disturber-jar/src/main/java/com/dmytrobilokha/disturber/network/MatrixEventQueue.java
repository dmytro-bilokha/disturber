package com.dmytrobilokha.disturber.network;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The class represents container for events queue. It is accessed from worker threads and from the FX application thread
 */
public class MatrixEventQueue {

    private final Queue<String> eventQueue = new ConcurrentLinkedQueue<>();
    private final Runnable newEventCallback;

    MatrixEventQueue(Runnable newEventCallback) {
        this.newEventCallback = newEventCallback;
    }

    void triggerEventCallback() {
        newEventCallback.run();
    }

    void addEvent(String event) {
        eventQueue.add(event);
    }

    void addEvents(Collection<String> events) {
        eventQueue.addAll(events);
    }

    String pollEvent() {
        return eventQueue.poll();
    }

}
