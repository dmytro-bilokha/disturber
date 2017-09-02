package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The matrix event DTO class
 */
public class EventDto {

    @JsonProperty(value = "content", required = true)
    private EventContentDto content;
    @JsonProperty(value = "prev_content", required = false)
    private EventContentDto previousContent;    //TODO: investigate why in the sync response example this is present, but in
                                                //the documentation (6.2.1) -- no???
    @JsonProperty(value = "origin_server_ts", required = true)
    private Long serverTimestamp;
    @JsonProperty(value = "sender", required = true)
    private String sender;
    @JsonProperty(value = "type", required = true)
    private String type;
    @JsonProperty(value = "age", required = true)
    private Long age;       //TODO: investigate why in the sync response example this is present, but in
                            //the documentation (6.2.1) -- no???
    @JsonProperty(value = "unsigned", required = true)
    private UnsignedDto unsigned;
    @JsonProperty(value = "state_key", required = false)
    private String stateKey;
    @JsonProperty(value = "event_id", required = true)
    private String eventId; //TODO: investigate why in the sync response example this is present, but in
                            //the documentation (6.2.1) -- no???
    @JsonProperty(value = "txn_id", required = true)
    private String txnId; //TODO: investigate why in the sync response example this is present, but in
    //the documentation (6.2.1) -- no???
    @JsonProperty(value = "membership", required = true)
    private String membership; //TODO: investigate why in the sync response example this is present, but in
    //the documentation (6.2.1) -- no???


    public Long getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(Long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public UnsignedDto getUnsigned() {
        return unsigned;
    }

    public void setUnsigned(UnsignedDto unsigned) {
        this.unsigned = unsigned;
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    public EventContentDto getContent() {
        return content;
    }

    public void setContent(EventContentDto content) {
        this.content = content;
    }

    public EventContentDto getPreviousContent() {
        return previousContent;
    }

    public void setPreviousContent(EventContentDto previousContent) {
        this.previousContent = previousContent;
    }
}
