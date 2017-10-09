package com.dmytrobilokha.disturber.network;

class ConnectionRetryStateHolder {

    private final int startPause;
    private int totalWaitTime;
    private int pauseTime;
    private boolean active;

    ConnectionRetryStateHolder(int startPause) {
        if (startPause < 2)
            throw new IllegalArgumentException("Pause time should be at least 2 ms, but got " + startPause);
        this.startPause = startPause;
        reset();
    }

    synchronized int pauseTime() {
        pauseTime = 2 * pauseTime;
        totalWaitTime += pauseTime;
        return pauseTime;
    }

    synchronized void reset() {
        totalWaitTime = 0;
        pauseTime = startPause / 2;
        active = true;
    }

    synchronized void stop() {
        active = false;
    }

    synchronized int getTotalWaitTime() {
        return totalWaitTime;
    }

    synchronized boolean isActive() {
        return active;
    }

}
