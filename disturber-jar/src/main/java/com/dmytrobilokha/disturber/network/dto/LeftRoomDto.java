package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The matrix Left Room DTO
 */
public class LeftRoomDto {

    @JsonProperty(value = "timeline", required = true)
    private TimelineDto timeline;
    @JsonProperty(value = "state", required = true)
    private StateDto state;

    public TimelineDto getTimeline() {
        return timeline;
    }

    public void setTimeline(TimelineDto timeline) {
        this.timeline = timeline;
    }

    public StateDto getState() {
        return state;
    }

    public void setState(StateDto state) {
        this.state = state;
    }

}
