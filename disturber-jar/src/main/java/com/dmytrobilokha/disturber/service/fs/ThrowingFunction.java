package com.dmytrobilokha.disturber.service.fs;

/**
 * Function which can throw exception
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;

}
