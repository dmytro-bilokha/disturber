package com.dmytrobilokha.disturber.appeventbus;

import java.lang.ref.WeakReference;

/**
 * The class extends WeakReference to override hashCode and equals methods. This is required to avoid existing two
 * weak references to the same object inside one Set.
 */
public class CustomizedWeakReference<T> extends WeakReference<T> {

    CustomizedWeakReference(T referent) {
        super(referent);
    }

    @Override
    public int hashCode() {
        return get() == null ? 0 : get().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        CustomizedWeakReference other = (CustomizedWeakReference) obj;
        if (this.get() == null || other.get() == null)
            return false;
        return get().equals(other.get());
    }

}
