package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The matrix Unread Notification Counts DTO
 */
public class UnreadNotificationCountsDto {

    @JsonProperty(value = "highlight_count", required = true)
    private Integer highlightCount;
    @JsonProperty(value = "notification_count", required = true)
    private Integer notificationCount;

    public Integer getHighlightCount() {
        return highlightCount;
    }

    public void setHighlightCount(Integer highlightCount) {
        this.highlightCount = highlightCount;
    }

    public Integer getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(Integer notificationCount) {
        this.notificationCount = notificationCount;
    }

}
