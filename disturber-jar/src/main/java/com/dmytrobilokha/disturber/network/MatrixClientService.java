package com.dmytrobilokha.disturber.network;


import com.dmytrobilokha.disturber.config.account.AccountConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by dimon on 13.08.17.
 */
@ApplicationScoped
public class MatrixClientService {

    private final Runnable eventCallback = () -> Platform.runLater(this::eventCallback);
    private final MatrixEventQueue eventQueue = new MatrixEventQueue(eventCallback);
    private final ObservableList<String> messageList = FXCollections.observableArrayList("Test1", "Test2", "Test3");

    public MatrixClientService() {
        //Empty no-args constructor to keep CDI framework happy
    }

    public ObservableList<String> connect(AccountConfig accountConfig) {
        new SynchronizeMessageService(accountConfig, eventQueue).start();
        return messageList;
    }

    private void eventCallback() {
        String message;
        while ((message = eventQueue.pollEvent()) != null) {
            messageList.add(message);
        }
    }

}
