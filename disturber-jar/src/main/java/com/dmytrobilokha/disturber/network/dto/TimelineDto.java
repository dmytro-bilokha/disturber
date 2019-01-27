package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * The matrix Timeline DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineDto {

    @JsonProperty(value = "limited", required = true)
    private Boolean limited;
    @JsonProperty(value = "prev_batch", required = true)
    private String previousBatch;
    @JsonProperty(value = "events", required = true)
    private List<EventDto> events = new ArrayList<>();

    public Boolean getLimited() {
        return limited;
    }

    public void setLimited(Boolean limited) {
        this.limited = limited;
    }

    public String getPreviousBatch() {
        return previousBatch;
    }

    public void setPreviousBatch(String previousBatch) {
        this.previousBatch = previousBatch;
    }

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }
}
