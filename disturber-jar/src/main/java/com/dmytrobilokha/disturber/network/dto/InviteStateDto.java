package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * The matrix InviteState DTO
 */
public class InviteStateDto {

    @JsonProperty(value = "events", required = true)
    List<EventDto> events = new ArrayList<>();

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }

}
