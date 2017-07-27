package com.dmytrobilokha.disturber.stevent;

import javax.enterprise.context.ApplicationScoped;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class StEventBus {

    private Thread initThread = Thread.currentThread();
    private Map<StEvent.Type, Set<WeakReference<StEventListener>>> listenersMap
            = new EnumMap<>(StEvent.Type.class);

    public void subscribe(StEventListener listener, StEvent.Type... types) {
        for (StEvent.Type type : types) {
            subscribe(listener, type);
        }
    }

    public void subscribe(StEventListener listener, StEvent.Type type) {
        Set<WeakReference<StEventListener>> listeners = listenersMap.computeIfAbsent(type, eventType -> new HashSet<>());
        Iterator<WeakReference<StEventListener>> listenersIterator = listeners.iterator();
        while(listenersIterator.hasNext()) {
            getReferenceOrRemoveDead(listenersIterator);
        }
        listeners.add(new WeakReference<>(listener));
    }

    private StEventListener getReferenceOrRemoveDead(Iterator<WeakReference<StEventListener>> iterator) {
        StEventListener listener = iterator.next().get();
        if (listener == null)
            iterator.remove();
        return listener;
    }

    public int fire(StEvent event) {
        if (initThread != Thread.currentThread())
            throw new IllegalStateException("Event bus has been initialized from thread '" +  initThread
                        + "', but event is fired from another thread '" + Thread.currentThread() + '\'');
        Set<WeakReference<StEventListener>> registeredListeners = listenersMap.get(event.getType());
        if (registeredListeners == null)
            return 0;
        int notifiedListeners = 0;
        Iterator<WeakReference<StEventListener>> listenersIterator = registeredListeners.iterator();
        while(listenersIterator.hasNext()) {
            StEventListener listener = getReferenceOrRemoveDead(listenersIterator);
            if (listener != null) {
                listener.onStEvent(event);
                notifiedListeners++;
            }
        }
        return notifiedListeners;
    }

}
