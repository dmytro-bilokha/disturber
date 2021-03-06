package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The matrix Account Data DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDataDto {

    @JsonProperty(value = "events", required = true)
    List<EventDto> events;

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }

}
