package com.dmytrobilokha.disturber.network;

import java.io.IOException;

/**
 * Supplier which may throw exception
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

    T get() throws IOException;

}
