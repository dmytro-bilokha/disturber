package com.dmytrobilokha.disturber.config.account;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * The class represents account settings
 */
public final class AccountConfig {

    private final String userId;
    private final String serverAddress;
    private final String login;
    private final String password;
    private final int betweenSyncPause;
    private final int syncTimeout;
    private final int networkTimeout;
    private final ProxyServer proxyServer;

    private AccountConfig(Builder builder, ProxyServer proxyServer) {
        this.userId = builder.userId;
        this.serverAddress = builder.serverAddress;
        this.login = builder.login;
        this.password = builder.password;
        this.betweenSyncPause = builder.betweenSyncPause;
        this.syncTimeout = builder.syncTimeout;
        this.networkTimeout = builder.networkTimeout;
        this.proxyServer = proxyServer;
    }

    static Builder newBuilder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public int getBetweenSyncPause() {
        return betweenSyncPause;
    }

    public int getSyncTimeout() {
        return syncTimeout;
    }

    public int getNetworkTimeout() {
        return networkTimeout;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountConfig that = (AccountConfig) o;
        return betweenSyncPause == that.betweenSyncPause &&
                syncTimeout == that.syncTimeout &&
                networkTimeout == that.networkTimeout &&
                Objects.equals(serverAddress, that.serverAddress) &&
                Objects.equals(login, that.login) &&
                Objects.equals(password, that.password) &&
                Objects.equals(proxyServer, that.proxyServer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverAddress, login);
    }

    @Override
    public String toString() {
        return "AccountConfig{" +
                "userId='" + userId + '\'' +
                ", serverAddress='" + serverAddress + '\'' +
                ", login='" + login + '\'' +
                ", betweenSyncPause=" + betweenSyncPause +
                ", syncTimeout=" + syncTimeout +
                ", networkTimeout=" + networkTimeout +
                ", proxyServer=" + proxyServer +
                '}';
    }

    static class Builder {
        private String userId;
        private String serverAddress;
        private String login;
        private String password;
        private int betweenSyncPause;
        private int syncTimeout;
        private int networkTimeout;
        private String proxyHost;
        private int proxyPort;

        Builder serverAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        Builder login(String login) {
            this.login = login;
            return this;
        }

        Builder password(String password) {
            this.password = password;
            return this;
        }

        Builder betweenSyncPause(int betweenSyncPause) {
            this.betweenSyncPause = betweenSyncPause;
            return this;
        }

        Builder syncTimeout(int syncTimeout) {
            this.syncTimeout = syncTimeout;
            return this;
        }

        Builder networkTimeout(int networkTimeout) {
            this.networkTimeout = networkTimeout;
            return this;
        }

        Builder proxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
            return this;
        }

        Builder proxyPort(int proxyPort) {
            this.proxyPort = proxyPort;
            return this;
        }

        private String buildUserId() throws MalformedURLException {
            return "@" + login + ':' + new URL(serverAddress).getHost();
        }

        AccountConfig build() {
            validate();
            try {
                userId = buildUserId();
            } catch (MalformedURLException ex) {
                throw new IllegalStateException("Failed to build userId from serverAddress='" + serverAddress + '\'', ex);
            }
            if (proxyHost == null || proxyHost.isEmpty())
                return new AccountConfig(this, null);
            return new AccountConfig(this, new ProxyServer(proxyHost, proxyPort));
        }

        private void validate() {
            validateValueProvided(serverAddress, "Server address");
            validateValueProvided(login, "Login");
            validateValueProvided(password, "Password");
            validateIntInRange(betweenSyncPause, 1, 300000, "Pause between server synchronization");
            validateIntInRange(syncTimeout, 1, 50000, "Synchronization server timeout");
            validateIntInRange(networkTimeout, 1000, 100000, "Network timeout");
            if (networkTimeout <= syncTimeout)
                throw new IllegalStateException("Network timeout set as " + networkTimeout + " and sync timeout is "
                                        + syncTimeout + " but network timeout should be less than sync timeout");
            if (proxyHost != null && !proxyHost.isEmpty())
                validateIntInRange(proxyPort, 1, 65535, "Proxy port");
        }

        private void validateValueProvided(String value, String msgFieldName) {
            if (value == null || value.isEmpty())
                throw new IllegalStateException(msgFieldName + " must be provided");
        }

        private void validateIntInRange(int value, int lowestValid, int greatestValid, String msgFieldName) {
            if (value < lowestValid || value > greatestValid)
                throw new IllegalStateException(msgFieldName + " must be in range from " + lowestValid
                            + " to " + greatestValid + ", but got " + value + " instead");
        }
    }
}
