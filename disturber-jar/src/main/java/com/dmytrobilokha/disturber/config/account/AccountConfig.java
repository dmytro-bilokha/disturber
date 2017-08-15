package com.dmytrobilokha.disturber.config.account;

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
}
