package com.dmytrobilokha.disturber.service.network;

/**
 * The class represents network connection configuration
 */
public class NetworkConnectionConfig {

    private final int connectionTimeout;
    private final int connectionInterval;

    public NetworkConnectionConfig(int connectionTimeout, int connectionInterval) {
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
