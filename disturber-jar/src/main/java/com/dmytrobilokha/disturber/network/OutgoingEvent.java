package com.dmytrobilokha.disturber.network;

class OutgoingEvent {

    private final Type type;
    private final String roomId;
    private final String messageText;
    private final String localId;

    private OutgoingEvent(Type type, String roomId, String messageText, String localId) {
        this.type = type;
        this.roomId = roomId;
        this.messageText = messageText;
        this.localId = localId;
    }

    static OutgoingEvent createMessageEvent(String roomId, String messageText, String localId) {
        return new OutgoingEvent(Type.MESSAGE, roomId, messageText, localId);
    }

    static OutgoingEvent createJoinEvent(String roomId) {
        return new OutgoingEvent(Type.JOIN, roomId, "", "");
    }

    Type getType() {
        return type;
    }

    String getRoomId() {
        return roomId;
    }

    String getMessageText() {
        return messageText;
    }

    String getLocalId() {
        return localId;
    }

    public enum Type {
        MESSAGE, JOIN;
    }

    @Override
    public String toString() {
        return "OutgoingEvent{" +
                "type=" + type +
                ", roomId='" + roomId + '\'' +
                ", messageText='" + messageText + '\'' +
                ", localId='" + localId + '\'' +
                '}';
    }
}
