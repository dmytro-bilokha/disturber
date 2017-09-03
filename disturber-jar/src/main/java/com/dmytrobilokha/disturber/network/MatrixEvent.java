package com.dmytrobilokha.disturber.network;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * The class represents Matrix event
 */
public final class MatrixEvent {

    private final String userId;
    private final String roomId;
    private final String sender;
    private final String contentType;
    private final String content;
    private final LocalDateTime serverTimestamp;

    private MatrixEvent(Builder builder) {
        userId = builder.userId;
        roomId = builder.roomId;
        sender = builder.sender;
        contentType = builder.contentType;
        content = builder.content;
        serverTimestamp = builder.serverTimestamp;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "" + serverTimestamp + '|' + userId + '|' + roomId + '|' + sender + '|' + contentType + '|' + content;
    }

    public static class Builder {

        private String userId;
        private String roomId;
        private String sender;
        private String contentType;
        private String content;
        private LocalDateTime serverTimestamp;

        private Builder() {
            //private constructor to restrict instantiation
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder serverTimestamp(long serverTimestamp) {
            this.serverTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(serverTimestamp), ZoneId.of("UTC"));
            return this;
        }

        public MatrixEvent build() {
            if (userId == null || roomId == null)
                throw new IllegalStateException("Fields userId and roomId should not be null");
            return new MatrixEvent(this);
        }
    }

}
