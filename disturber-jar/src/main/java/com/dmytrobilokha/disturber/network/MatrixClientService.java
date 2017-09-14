package com.dmytrobilokha.disturber.network;


import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.config.account.AccountConfig;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The service responsible for keeping in sync with Matrix accounts. It aggregates UI-related stuff with
 * Matrix API stuff.
 */
@ApplicationScoped
public class MatrixClientService {

    private final Map<AccountConfig, MatrixSynchronizer> connectedAccounts = new HashMap<>();

    private CrossThreadEventQueue eventQueue;

    private AppEventBus appEventBus;
    private MatrixSynchronizerFactory synchronizerFactory;

    protected MatrixClientService() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    public MatrixClientService(AppEventBus appEventBus, MatrixSynchronizerFactory synchronizerFactory) {
        this.appEventBus = appEventBus;
        this.synchronizerFactory = synchronizerFactory;
        this.eventQueue = synchronizerFactory.createCrossThreadEventQueue(this::eventCallback);
    }

    public void connect(Collection<AccountConfig> accountConfigs) {
        for (AccountConfig accountConfig : accountConfigs) {
            if (!connectedAccounts.containsKey(accountConfig)) {
                MatrixSynchronizer synchronizer = synchronizerFactory.createMatrixSynchronizer(accountConfig, eventQueue);
                connectedAccounts.put(accountConfig, synchronizer);
                synchronizer.start();
            }
        }
    }

    private void eventCallback() {
        AppEvent event;
        while ((event = eventQueue.pollEvent()) != null) {
            appEventBus.fire(event);
        }
    }

    @PreDestroy
    void shutDown() {
        connectedAccounts.values().forEach(MatrixSynchronizer::disconnect);
    }

}
