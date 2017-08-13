package com.dmytrobilokha.disturber.service.network;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Created by dimon on 13.08.17.
 */
public class SynchronizeMessageService extends Service<Void> {

    private final ObservableList<String> messageList;
    private final String baseUrl;

    SynchronizeMessageService(ObservableList<String> messageList, String baseUrl) {
        this.messageList = messageList;
        this.baseUrl = baseUrl;
    }

    @Override
    protected Task<Void> createTask() {
        return new SynchronizeMessageTask(messageList, baseUrl);
    }

}
