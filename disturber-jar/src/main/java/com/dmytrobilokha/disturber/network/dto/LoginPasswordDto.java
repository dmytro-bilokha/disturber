package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The DTO used to authentificate with login/password pair
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginPasswordDto {


    @JsonProperty(value = "user")
    private String login;
    @JsonProperty(value = "password")
    private String password;

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

    @JsonProperty(value = "type", access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return "m.login.password";
    }

}
