package com.dmytrobilokha.disturber.network;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Created by dimon on 13.08.17.
 */
public class SynchronizeMessageService extends Service<Void> {

    private final ObservableList<String> messageList;
    private final MatrixAccount matrixAccount;

    SynchronizeMessageService(ObservableList<String> messageList, MatrixAccount account) {
        this.messageList = messageList;
        this.matrixAccount = account;
    }

    @Override
    protected Task<Void> createTask() {
        return new SynchronizeMessageTask(messageList, matrixAccount);
    }

}
