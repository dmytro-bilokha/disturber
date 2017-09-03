package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Created by dimon on 13.08.17.
 */
public class SynchronizeMessageService extends Service<Void> {

    private final AccountConfig accountConfig;
    private final MatrixEventQueue eventQueue;

    SynchronizeMessageService(AccountConfig accountConfig, MatrixEventQueue eventQueue) {
        this.accountConfig = accountConfig;
        this.eventQueue = eventQueue;
    }

    @Override
    protected Task<Void> createTask() {
        return new MatrixClient(accountConfig, eventQueue, new MatrixApiConnector());
    }

}
