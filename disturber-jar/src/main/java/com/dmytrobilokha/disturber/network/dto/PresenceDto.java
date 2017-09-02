package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The matrix presence DTO
 */
public class PresenceDto {

    @JsonProperty(value = "events", required = true)
    List<EventDto> events;

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }

}