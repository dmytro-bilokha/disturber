package com.dmytrobilokha.disturber.config.account;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * DTO for AccountConfig for serializing to/from XML file
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "account")
public class AccountConfigDto {

    private String serverAddress;
    private String login;
    private String password;

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountConfigDto that = (AccountConfigDto) o;
        return Objects.equals(serverAddress, that.serverAddress) &&
                Objects.equals(login, that.login) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverAddress, login);
    }

    @Override
    public String toString() {
        return "AccountConfigDto{" +
                "serverAddress='" + serverAddress + '\'' +
                ", login='" + login + '\'' +
                '}';
    }

}
