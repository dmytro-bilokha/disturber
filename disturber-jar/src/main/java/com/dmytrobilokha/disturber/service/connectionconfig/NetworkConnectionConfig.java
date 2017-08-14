package com.dmytrobilokha.disturber.service.connectionconfig;

/**
 * The class represents network connection configuration
 */
public class NetworkConnectionConfig {

    private final int connectionTimeout;
    private final int connectionInterval;

    NetworkConnectionConfig(int connectionTimeout, int connectionInterval) {
        this.connectionTimeout = connectionTimeout;
        this.connectionInterval = connectionInterval;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getConnectionInterval() {
        return connectionInterval;
    }

}
