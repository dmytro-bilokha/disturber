package com.dmytrobilokha.disturber.stevent;

@FunctionalInterface
public interface StEventListener<T> {

    void onStEvent(StEvent<T> stEvent);

}
