package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * The matrix presence DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresenceDto {

    @JsonProperty(value = "events", required = true)
    List<EventDto> events = new ArrayList<>();

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }

}
