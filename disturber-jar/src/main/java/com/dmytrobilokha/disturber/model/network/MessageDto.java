package com.dmytrobilokha.disturber.model.network;

public class MessageDto {

    String type;
    MessagePayloadDto value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MessagePayloadDto getValue() {
        return value;
    }

    public void setValue(MessagePayloadDto value) {
        this.value = value;
    }
}
