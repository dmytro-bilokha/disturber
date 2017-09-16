package com.dmytrobilokha.disturber.util;

/**
 * Function which can throw exception
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;

}
