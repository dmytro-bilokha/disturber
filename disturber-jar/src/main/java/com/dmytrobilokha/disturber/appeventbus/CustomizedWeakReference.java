package com.dmytrobilokha.disturber.appeventbus;

import java.lang.ref.WeakReference;

/**
 * The class extends WeakReference to override hashCode and equals methods. This is required to avoid existing two
 * weak references to the same object inside one Set.
 */
public class CustomizedWeakReference<T> extends WeakReference<T> {

    public CustomizedWeakReference(T referent) {
        super(referent);
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return get().equals(obj);
    }

}
