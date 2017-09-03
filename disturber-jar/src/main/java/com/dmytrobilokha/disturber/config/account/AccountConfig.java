package com.dmytrobilokha.disturber.config.account;

import java.util.Objects;

/**
 * The class represents account settings
 */
public class AccountConfig {

    private final String serverAddress;
    private final String login;
    private final String password;
    private final int betweenSyncPause;
    private final int syncTimeout;
    private final int networkTimeout;

    AccountConfig(String serverAddress, String login, String password, int betweenSyncPause, int syncTimeout, int networkTimeout) {
        this.serverAddress = serverAddress;
        this.login = login;
        this.password = password;
        this.betweenSyncPause = betweenSyncPause;
        this.syncTimeout = syncTimeout;
        this.networkTimeout = networkTimeout;
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
}
