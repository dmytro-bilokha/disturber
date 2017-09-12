package com.dmytrobilokha.disturber.appeventbus;

/**
 * The class represents application event container
 */
public final class AppEvent<K, T> {

    private final AppEventType type;
    private final K classifier;
    private final T payload;

    private AppEvent(AppEventType type, K classifier, T payload) {
        if (type == null)
            throw new IllegalArgumentException("Type must not be null");
        type.validate(classifier, payload);
        this.type = type;
        this.classifier = classifier;
        this.payload = payload;
    }

    public static AppEvent ofType(AppEventType type) {
        return new AppEvent(type, null, null);
    }

    public static <X> AppEvent<Void, X> withPayload(AppEventType type, X payload) {
        return new AppEvent<>(type, null, payload);
    }

    public static <L> AppEvent<L, Void> withClassifier(AppEventType type, L classifier) {
        return new AppEvent<>(type, classifier, null);
    }

    public static <L, X> AppEvent<L, X> withClassifierAndPayload(AppEventType type, L classifier, X payload) {
        return new AppEvent<>(type, classifier, payload);
    }

    public AppEventType getType() {
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

}
