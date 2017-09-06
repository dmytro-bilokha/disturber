package com.dmytrobilokha.disturber.network;

import javafx.application.Platform;

import javax.enterprise.context.Dependent;

@Dependent
class RunLaterWrapper {

    Runnable wrap(Runnable callback) {
        return () -> Platform.runLater(callback);
    }

}
