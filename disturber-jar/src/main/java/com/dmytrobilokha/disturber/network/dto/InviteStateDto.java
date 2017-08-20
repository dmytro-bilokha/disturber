package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The matrix InviteState DTO
 */
public class InviteStateDto {

    @JsonProperty(value = "events", required = true)
    List<EventDto> events;

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }

}
