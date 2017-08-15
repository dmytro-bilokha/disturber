package com.dmytrobilokha.disturber.service.network;

import com.dmytrobilokha.disturber.config.connection.NetworkConnectionConfig;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Created by dimon on 13.08.17.
 */
public class SynchronizeMessageService extends Service<Void> {

    private final ObservableList<String> messageList;
    private final String baseUrl;
    private final NetworkConnectionConfig connectionConfig;

    SynchronizeMessageService(ObservableList<String> messageList, String baseUrl, NetworkConnectionConfig connectionConfig) {
        this.messageList = messageList;
        this.baseUrl = baseUrl;
        this.connectionConfig = connectionConfig;
    }

    @Override
    protected Task<Void> createTask() {
        return new SynchronizeMessageTask(messageList, baseUrl, connectionConfig);
    }

}
