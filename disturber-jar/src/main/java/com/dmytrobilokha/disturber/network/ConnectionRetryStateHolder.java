package com.dmytrobilokha.disturber.network;

class ConnectionRetryStateHolder {

    private final int startPause;
    private int totalWaitTime;
    private boolean active;

    ConnectionRetryStateHolder(int startPause) {
        if (startPause < 2)
            throw new IllegalArgumentException("Pause time should be at least 2 ms, but got " + startPause);
        this.startPause = startPause;
        reset();
    }

    synchronized int pauseTime() {
        if (!active)
            return startPause;
        int pauseTime = totalWaitTime == 0 ? startPause : totalWaitTime;
        totalWaitTime += pauseTime;
        return pauseTime;
    }

    synchronized void reset() {
        totalWaitTime = 0;
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
