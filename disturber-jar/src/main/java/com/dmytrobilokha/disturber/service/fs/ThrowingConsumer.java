package com.dmytrobilokha.disturber.service.fs;

/**
 * The class to replace standard JDK Consumer to be able to throw exception
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {

    void accept(T t) throws Exception;

}
