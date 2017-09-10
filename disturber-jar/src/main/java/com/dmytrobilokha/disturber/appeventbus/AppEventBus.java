package com.dmytrobilokha.disturber.appeventbus;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@ApplicationScoped
public class AppEventBus {

    private final Map<TypeClassifierKey, Set<CustomizedWeakReference<AppEventListener>>> listenersMap = new HashMap<>();

    public void subscribe(AppEventListener listener, AppEventType type) {
        subscribe(listener, type, null);
    }

    public void subscribe(AppEventListener listener, AppEventType type, Object classifier) {
        TypeClassifierKey eventKey = new TypeClassifierKey(type, classifier);
        Set<CustomizedWeakReference<AppEventListener>> listeners = listenersMap.computeIfAbsent(eventKey, k -> new HashSet<>());
        cleanAndConsumeListeners(listeners, null);
        listeners.add(new CustomizedWeakReference<>(listener));
    }

    public void unsubscribe(AppEventListener listener, AppEventType type) {
        unsubscribe(listener, type, null);
    }

    public void unsubscribe(AppEventListener listener, AppEventType type, Object classifier) {
        TypeClassifierKey eventKey = new TypeClassifierKey(type, classifier);
        Set<CustomizedWeakReference<AppEventListener>> listeners = listenersMap.get(eventKey);
        if (listeners == null)
            return;
        listeners.remove(new CustomizedWeakReference<>(listener));
    }

    public void fire(AppEvent appEvent) {
        TypeClassifierKey eventKey = TypeClassifierKey.of(appEvent);
        Set<CustomizedWeakReference<AppEventListener>> registeredListeners = listenersMap.get(eventKey);
        notifyListeners(registeredListeners, appEvent);
        if (!eventKey.isGeneral()) {
            Set<CustomizedWeakReference<AppEventListener>> registeredGeneralListeners = listenersMap.get(eventKey.getGeneralized());
            notifyListeners(registeredGeneralListeners, appEvent);
        }
    }

    private void notifyListeners(Set<CustomizedWeakReference<AppEventListener>> registeredListeners, AppEvent appEvent) {
        cleanAndConsumeListeners(registeredListeners, listener -> listener.onAppEvent(appEvent));
    }

    private void cleanAndConsumeListeners(Set<CustomizedWeakReference<AppEventListener>> registeredListeners
            , Consumer<AppEventListener> consumer) {
        if (registeredListeners != null) {
            for (Iterator<CustomizedWeakReference<AppEventListener>> listenersIterator
                 = registeredListeners.iterator(); listenersIterator.hasNext();) {
                AppEventListener listener = getReferenceOrRemoveDead(listenersIterator);
                if (consumer != null && listener != null) {
                    consumer.accept(listener);
                }
            }
        }
    }

    private AppEventListener getReferenceOrRemoveDead(Iterator<CustomizedWeakReference<AppEventListener>> iterator) {
        AppEventListener listener = iterator.next().get();
        if (listener == null)
            iterator.remove();
        return listener;
    }

    private static final class TypeClassifierKey {
        private final AppEventType type;
        private final Object classifier;

        private TypeClassifierKey(AppEventType type, Object classifier) {
            this.type = type;
            this.classifier = classifier;
        }

        private static TypeClassifierKey of(AppEvent event) {
            return new TypeClassifierKey(event.getType(), event.getClassifier());
        }

        private boolean isGeneral() {
            return classifier == null;
        }

        private TypeClassifierKey getGeneralized() {
            return new TypeClassifierKey(type, null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeClassifierKey that = (TypeClassifierKey) o;
            return type == that.type &&
                    Objects.equals(classifier, that.classifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, classifier);
        }

        @Override
        public String toString() {
            return "TypeClassifierKey{" +
                    "type=" + type +
                    ", classifier=" + classifier +
                    '}';
        }
    }

}
