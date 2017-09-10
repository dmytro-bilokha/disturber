package com.dmytrobilokha.disturber.commonmodel;

import java.util.Objects;

/**
 * The class represents account-room identity key
 */
public final class RoomKey {

    private final String userId;
    private final String roomId;

    public RoomKey(String userId) {
        this(userId, null);
    }

    public RoomKey(String userId, String roomId) {
        if (userId == null)
            throw new IllegalArgumentException("UserId should not be null");
        this.userId = userId;
        this.roomId = roomId;
    }

    public boolean hasRoomId() {
        return roomId != null;
    }

    public String getUserId() {
        return userId;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomKey roomKey = (RoomKey) o;
        return Objects.equals(userId, roomKey.userId) &&
                Objects.equals(roomId, roomKey.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roomId);
    }

    @Override
    public String toString() {
        return "RoomKey{" +
                "userId='" + userId + '\'' +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
