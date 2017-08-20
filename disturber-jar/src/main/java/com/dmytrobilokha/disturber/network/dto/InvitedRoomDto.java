package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The matrix Invited Room DTO
 */
public class InvitedRoomDto {

    @JsonProperty(value = "invite_state", required = true)
    private InviteStateDto inviteState;

    public InviteStateDto getInviteState() {
        return inviteState;
    }

    public void setInviteState(InviteStateDto inviteState) {
        this.inviteState = inviteState;
    }

}
