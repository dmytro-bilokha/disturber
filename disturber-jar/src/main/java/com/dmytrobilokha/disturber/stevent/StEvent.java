package com.dmytrobilokha.disturber.stevent;

public class StEvent<T> {

    private final Type type;
    private final T payload;

    private StEvent(Type type, T payload) {
        if (type == null)
            throw new IllegalArgumentException("Type must not be null");
        if (payload != null && !type.payloadClass().isAssignableFrom(payload.getClass()))
            throw new IllegalArgumentException("Got illegal payload object of class '" + payload.getClass()
                    +"', but for event of type '" + type + "' expected payload class is '" + type.payloadClass() + '\'');
        this.type = type;
        this.payload = payload;
    }

    public static <X> StEvent<X> of(Type type, X payload) {
        return new StEvent<>(type, payload);
    }

    public Type getType() {
        return type;
    }

    public T getPayload() {
        return payload;
    }

    public enum Type {
        USER_NAME_CHANGED(String.class);

        Class clazz;

        Type(Class clazz) {
            this.clazz = clazz;
        }

        public Class payloadClass() {
            return clazz;
        }
    }
}
