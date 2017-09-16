package com.dmytrobilokha.disturber.commonmodel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * The class represents Matrix event
 */
public final class MatrixEvent {

    private final String sender;
    private final String contentType;
    private final String content;
    private final LocalDateTime serverTimestamp;

    private MatrixEvent(Builder builder) {
        sender = builder.sender;
        contentType = builder.contentType;
        content = builder.content;
        serverTimestamp = builder.serverTimestamp;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getSender() {
        return sender;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getServerTimestamp() {
        return serverTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatrixEvent event = (MatrixEvent) o;
        return Objects.equals(sender, event.sender) &&
                Objects.equals(contentType, event.contentType) &&
                Objects.equals(content, event.content) &&
                Objects.equals(serverTimestamp, event.serverTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, contentType, content);
    }

    @Override
    public String toString() {
        return "" + serverTimestamp + '|' + sender + '|' + contentType + '|' + content;
    }

    public static class Builder {

        private String sender;
        private String contentType;
        private String content;
        private LocalDateTime serverTimestamp;

        private Builder() {
            //private constructor to restrict instantiation
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
            validate();
            return new MatrixEvent(this);
        }

        private void validate() {
            if (this.serverTimestamp == null)
                throw new IllegalStateException("Unable to build, because serverTimestamp has not been set");
        }

    }

}
