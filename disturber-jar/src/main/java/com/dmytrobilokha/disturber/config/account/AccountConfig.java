package com.dmytrobilokha.disturber.config.account;

import java.util.Objects;

/**
 * The class represents account settings
 */
public final class AccountConfig {

    private final String serverAddress;
    private final String login;
    private final String password;
    private final int betweenSyncPause;
    private final int syncTimeout;
    private final int networkTimeout;

    private AccountConfig(Builder builder) {
        this.serverAddress = builder.serverAddress;
        this.login = builder.login;
        this.password = builder.password;
        this.betweenSyncPause = builder.betweenSyncPause;
        this.syncTimeout = builder.syncTimeout;
        this.networkTimeout = builder.networkTimeout;
    }

    static Builder newBuilder() {
        return new Builder();
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

    @Override
    public String toString() {
        return "AccountConfig{" +
                "serverAddress='" + serverAddress + '\'' +
                ", login='" + login + '\'' +
                ", betweenSyncPause=" + betweenSyncPause +
                ", syncTimeout=" + syncTimeout +
                ", networkTimeout=" + networkTimeout +
                '}';
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
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverAddress, login);
    }

    static class Builder {
        private String serverAddress;
        private String login;
        private String password;
        private int betweenSyncPause;
        private int syncTimeout;
        private int networkTimeout;

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

        AccountConfig build() {
            return new AccountConfig(this);
        }

    }
}
