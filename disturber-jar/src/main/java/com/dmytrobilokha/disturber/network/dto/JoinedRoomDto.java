package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The matrix Joined Room DTO
 */
public class JoinedRoomDto {

    @JsonProperty(value = "unread_notifications", required = true)
    private UnreadNotificationCountsDto unreadNotificationCounts;
    @JsonProperty(value = "timeline", required = true)
    private TimelineDto timeline;
    @JsonProperty(value = "state", required = true)
    private StateDto state;
    @JsonProperty(value = "account_data", required = true)
    private AccountDataDto accountData;
    @JsonProperty(value = "ephemeral", required = true)
    private EphemeralDto ephemeral;

    public UnreadNotificationCountsDto getUnreadNotificationCounts() {
        return unreadNotificationCounts;
    }

    public void setUnreadNotificationCounts(UnreadNotificationCountsDto unreadNotificationCounts) {
        this.unreadNotificationCounts = unreadNotificationCounts;
    }

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

    public AccountDataDto getAccountData() {
        return accountData;
    }

    public void setAccountData(AccountDataDto accountData) {
        this.accountData = accountData;
    }

    public EphemeralDto getEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(EphemeralDto ephemeral) {
        this.ephemeral = ephemeral;
    }

}
