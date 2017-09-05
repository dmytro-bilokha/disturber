package com.dmytrobilokha.disturber.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * The service to encapsulate FX platform related functionality
 */
@ApplicationScoped
public class PlatformService {

    public Runnable createRunLaterCallback(Runnable callback) {
        return () -> Platform.runLater(callback);
    }

    public <T> List<T> createList() {
        return FXCollections.observableArrayList();
    }

}
