package com.dmytrobilokha.disturber.network;


import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.config.account.AccountConfigService;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The service responsible for keeping in sync with Matrix accounts. It aggregates UI-related stuff with
 * Matrix API stuff.
 */
@ApplicationScoped
public class MatrixClientService {

    private final Map<AccountConfig, MatrixSynchronizer> connectedAccounts = new HashMap<>();

    private CrossThreadEventQueue eventQueue;

    private AccountConfigService accountConfigService;
    private AppEventBus appEventBus;

    protected MatrixClientService() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    public MatrixClientService(AccountConfigService accountConfigService, AppEventBus appEventBus
            , RunLaterWrapper runLaterWrapper) {
        this.accountConfigService = accountConfigService;
        this.appEventBus = appEventBus;
        this.eventQueue = new CrossThreadEventQueue(runLaterWrapper.wrap(this::eventCallback));
    }

    public void connect() throws AccountConfigAccessException {
        List<AccountConfig> configs = accountConfigService.getAccountConfigs();
        for (AccountConfig accountConfig : configs) {
            if (!connectedAccounts.containsKey(accountConfig)) {
                MatrixSynchronizer synchronizer = new MatrixSynchronizer(accountConfig, eventQueue, new MatrixApiConnector());
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
    public void shutDown() {
        connectedAccounts.values().forEach(MatrixSynchronizer::disconnect);
    }

}
