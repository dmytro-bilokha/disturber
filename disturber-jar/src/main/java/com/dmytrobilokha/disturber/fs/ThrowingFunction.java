package com.dmytrobilokha.disturber.fs;

/**
 * Function which can throw exception
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;

}
