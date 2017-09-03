package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * The class represents synchronizer which goal is to run Matrix Client
 */
public class MatrixSynchronizer extends Service<Void> {

    private final AccountConfig accountConfig;
    private final MatrixEventQueue eventQueue;

    MatrixSynchronizer(AccountConfig accountConfig, MatrixEventQueue eventQueue) {
        this.accountConfig = accountConfig;
        this.eventQueue = eventQueue;
    }

    @Override
    protected Task<Void> createTask() {
        return new MatrixClient(accountConfig, eventQueue, new MatrixApiConnector());
    }

}
