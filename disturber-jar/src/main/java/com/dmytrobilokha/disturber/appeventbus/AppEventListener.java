package com.dmytrobilokha.disturber.appeventbus;

@FunctionalInterface
public interface AppEventListener<K, T> {

    void onAppEvent(AppEvent<K, T> appEvent);

}
