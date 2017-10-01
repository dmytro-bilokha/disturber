package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import javafx.application.Platform;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
class MatrixSynchronizerFactory {

    private final ApiExceptionToSystemMessageConverter exceptionConverter;

    @Inject
    MatrixSynchronizerFactory(ApiExceptionToSystemMessageConverter exceptionConverter) {
        this.exceptionConverter = exceptionConverter;
    }

    MatrixSynchronizer createMatrixSynchronizer(AccountConfig accountConfig, CrossThreadEventQueue eventQueue) {
        return new MatrixSynchronizer(accountConfig, eventQueue, new MatrixApiConnector(), exceptionConverter);
    }

    CrossThreadEventQueue createCrossThreadEventQueue(Runnable onEventCallback) {
        return new CrossThreadEventQueue(() -> Platform.runLater(onEventCallback));
    }

}
