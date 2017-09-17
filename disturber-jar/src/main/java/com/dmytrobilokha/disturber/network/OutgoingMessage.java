package com.dmytrobilokha.disturber.network;

class OutgoingMessage {

    private final String roomId;
    private final String messageText;
    private final String localId;

    OutgoingMessage(String roomId, String messageText, String localId) {
        this.roomId = roomId;
        this.messageText = messageText;
        this.localId = localId;
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

}
