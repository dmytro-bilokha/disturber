package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * The matrix Rooms DTO
 */
public class RoomsDto {

    @JsonProperty(value = "leave", required = true)
    private Map<String, LeftRoomDto> leftRoomMap = new HashMap<>();
    @JsonProperty(value = "join", required = true)
    private Map<String, JoinedRoomDto> joinedRoomMap = new HashMap<>();
    @JsonProperty(value = "invite", required = true)
    private Map<String, InvitedRoomDto> invitedRoomMap = new HashMap<>();

    public Map<String, LeftRoomDto> getLeftRoomMap() {
        return leftRoomMap;
    }

    public void setLeftRoomMap(Map<String, LeftRoomDto> leftRoomMap) {
        this.leftRoomMap = leftRoomMap;
    }

    public Map<String, JoinedRoomDto> getJoinedRoomMap() {
        return joinedRoomMap;
    }

    public void setJoinedRoomMap(Map<String, JoinedRoomDto> joinedRoomMap) {
        this.joinedRoomMap = joinedRoomMap;
    }

    public Map<String, InvitedRoomDto> getInvitedRoomMap() {
        return invitedRoomMap;
    }

    public void setInvitedRoomMap(Map<String, InvitedRoomDto> invitedRoomMap) {
        this.invitedRoomMap = invitedRoomMap;
    }
}
