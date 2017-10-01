package com.dmytrobilokha.disturber;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import com.dmytrobilokha.disturber.viewcontroller.DialogButton;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Service to catch synchronization/login/send_message errors and handle them
 */
@ApplicationScoped
public class MatrixFailHandler {

    private final AppEventListener<AccountConfig, SystemMessage> failListener = this::onFail;
    private static final DialogButton[] RETRY_STOP = new DialogButton[]{DialogButton.RETRY, DialogButton.STOP};

    private AppEventBus eventBus;
    private ViewFactory viewFactory;
    private MatrixClientService matrixClientService;

    protected MatrixFailHandler() {
        //No-args constructor for CDI framework
    }

    @Inject
    public MatrixFailHandler(AppEventBus eventBus, ViewFactory viewFactory, MatrixClientService matrixClientService) {
        this.eventBus = eventBus;
        this.viewFactory = viewFactory;
        this.matrixClientService = matrixClientService;
        eventBus.subscribe(failListener, AppEventType.MATRIX_CONNECTION_FAILED);
        eventBus.subscribe(failListener, AppEventType.MATRIX_RESPONSE_FAILED);
    }

    private void onFail(AppEvent<AccountConfig, SystemMessage> failEvent) {
        SystemMessage failMessage = failEvent.getPayload();
        DialogButton userChoice = viewFactory.showErrorDialog(failMessage, RETRY_STOP);
        if (userChoice == DialogButton.RETRY)
            matrixClientService.setRetryOn(failEvent.getClassifier());
    }

    public void eagerInit(@Observes @Initialized(ApplicationScoped.class) Object initEvent) {
        //The methods does nothing. We need it just to ensure a CDI framework initializes the bean eagerly, so we won't
        //miss any fails and handle them all
    }
}
