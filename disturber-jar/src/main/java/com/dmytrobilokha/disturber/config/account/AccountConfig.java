package com.dmytrobilokha.disturber.config.account;

import java.util.Objects;

/**
 * The class represents account settings
 */
public class AccountConfig {

    private final String serverAddress;
    private final String login;
    private final String password;

    AccountConfig(String serverAddress, String login, String password) {
        this.serverAddress = serverAddress;
        this.login = login;
        this.password = password;
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

    @Override
    public String toString() {
        return "AccountConfig{" +
                "serverAddress='" + serverAddress + '\'' +
                ", login='" + login + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountConfig that = (AccountConfig) o;
        return Objects.equals(serverAddress, that.serverAddress) &&
                Objects.equals(login, that.login) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverAddress, login);
    }

}
