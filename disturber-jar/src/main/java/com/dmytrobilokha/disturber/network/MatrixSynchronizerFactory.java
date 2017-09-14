package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import javafx.application.Platform;

import javax.enterprise.context.Dependent;

@Dependent
class MatrixSynchronizerFactory {

    MatrixSynchronizerFactory() {
        //No args constructor for CDI framework
    }

    MatrixSynchronizer createMatrixSynchronizer(AccountConfig accountConfig, CrossThreadEventQueue eventQueue) {
        return new MatrixSynchronizer(accountConfig, eventQueue, new MatrixApiConnector());
    }

    CrossThreadEventQueue createCrossThreadEventQueue(Runnable onEventCallback) {
        return new CrossThreadEventQueue(() -> Platform.runLater(onEventCallback));
    }

}
