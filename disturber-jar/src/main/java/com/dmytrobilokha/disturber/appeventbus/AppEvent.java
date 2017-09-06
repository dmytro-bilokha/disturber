package com.dmytrobilokha.disturber.appeventbus;

import com.dmytrobilokha.disturber.network.RoomKey;

public final class AppEvent<K, T> {

    private final Type type;
    private final K classifier;
    private final T payload;

    private AppEvent(Type type, K classifier, T payload) {
        if (type == null)
            throw new IllegalArgumentException("Type must not be null");
        if (classifier != null && type.classifierClass == null)
            throw new IllegalArgumentException("Got classifier " + classifier + ", but for event of type '"
                    + type + "' classifier is not supported");
        if (classifier != null && !type.classifierClass.isAssignableFrom(classifier.getClass()))
            throw new IllegalArgumentException("Got illegal classifier object of class '" + classifier.getClass()
                    +"', but for event of type '" + type + "' expected classifier class is '" + type.classifierClass + '\'');
        if (payload != null && !type.payloadClass.isAssignableFrom(payload.getClass()))
            throw new IllegalArgumentException("Got illegal payload object of class '" + payload.getClass()
                    +"', but for event of type '" + type + "' expected payload class is '" + type.payloadClass + '\'');
        this.type = type;
        this.classifier = classifier;
        this.payload = payload;
    }

    public static <L, X> AppEvent<L, X> of(Type type, X payload) {
        return new AppEvent<>(type, null, payload);
    }

    public static <L, X> AppEvent<L, X> of(Type type, L classifier, X payload) {
        return new AppEvent<>(type, classifier, payload);
    }

    public static <L, X> AppEvent<L, X> empty(Type type) {
        return of(type, null);
    }

    public static <L, X> AppEvent<L, X> empty(Type type, L classifier) {
        return of(type, classifier, null);
    }

    public Type getType() {
        return type;
    }

    public K getClassifier() {
        return classifier;
    }

    public T getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "AppEvent{" +
                "type=" + type +
                ", classifier=" + classifier +
                ", payload=" + payload +
                '}';
    }

    public enum Type {
        MATRIX_NEW_ROOM_SYNCED(null, RoomKey.class)
        , USER_NAME_CHANGED(null, String.class);

        private final Class classifierClass;
        private final Class payloadClass;

        Type(Class classifierClass, Class payloadClass) {
            this.classifierClass = classifierClass;
            this.payloadClass = payloadClass;
        }
    }

}
