package com.dmytrobilokha.disturber.network;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The class represents container for events queue. It is accessed from worker threads and from the FX application thread
 */
public final class MatrixEventQueue {

    private final Queue<MatrixEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private final Runnable newEventCallback;

    MatrixEventQueue(Runnable newEventCallback) {
        this.newEventCallback = newEventCallback;
    }

    void triggerEventCallback() {
        newEventCallback.run();
    }

    void addEvent(MatrixEvent event) {
        eventQueue.add(event);
    }

    void addEvents(Collection<MatrixEvent> events) {
        eventQueue.addAll(events);
    }

    MatrixEvent pollEvent() {
        return eventQueue.poll();
    }

}
