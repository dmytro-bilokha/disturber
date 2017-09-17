package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendEventResponseDto {

    @JsonProperty(value = "event_id", required = true)
    private String eventId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "SendEventResponseDto{" +
                "eventId='" + eventId + '\'' +
                '}';
    }
}
