package com.dmytrobilokha.disturber.service.fs;

import java.io.IOException;

/**
 * The class to replace standard JDK Consumer to be able to throw exception
 */
@FunctionalInterface
public interface IoConsumer<T> {

    void accept(T t) throws IOException;

}
