package com.dmytrobilokha.disturber.config.account;

import java.util.Objects;

public class ProxyServer {

    private final String host;
    private final int port;

    ProxyServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyServer that = (ProxyServer) o;
        return port == that.port &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host);
    }

    @Override
    public String toString() {
        return "ProxyServer{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
