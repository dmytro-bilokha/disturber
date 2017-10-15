package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response sent on successful join room request
 */
public class JoinResponseDto {

    @JsonProperty(value = "room_id", required = true)
    public String roomId;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return "JoinResponseDto{" +
                "roomId='" + roomId + '\'' +
                '}';
    }
}
