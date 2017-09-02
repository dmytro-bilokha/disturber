package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

//TODO: investigate this entity and add more field, or may be introduce class hierarchy
/**
 * The class represents event content DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventContentDto {

    @JsonProperty(value = "membership", required = false)
    private String membership;
    @JsonProperty(value = "name", required = false)
    private String name;
    @JsonProperty(value = "presence", required = false)
    private String presence;
    @JsonProperty(value = "custom_config_key", required = false)
    private String customConfig; //TODO: investigate how to handle unknown properties in the content???
    @JsonProperty(value = "body", required = false)
    private String body;
    @JsonProperty(value = "msgtype", required = false)
    private String msgType;
    @JsonProperty(value = "user_ids", required = false)
    private List<String> userIds;

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public String getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(String customConfig) {
        this.customConfig = customConfig;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }


}
