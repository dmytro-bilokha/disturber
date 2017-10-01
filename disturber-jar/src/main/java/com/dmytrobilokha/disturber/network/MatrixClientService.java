package com.dmytrobilokha.disturber.network;


import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(MatrixClientService.class);

    private final Map<String, MatrixSynchronizer> connectedAccounts = new HashMap<>();
    private final AppEventListener<RoomKey, String> outgoingMessageListener = this::enqueueOutgoingMessage;

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
        appEventBus.subscribe(outgoingMessageListener, AppEventType.MATRIX_OUTGOING_MESSAGE);
    }

    public void connect(Collection<AccountConfig> accountConfigs) {
        for (AccountConfig accountConfig : accountConfigs) {
            if (!connectedAccounts.containsKey(accountConfig.getUserId())) {
                MatrixSynchronizer synchronizer = synchronizerFactory.createMatrixSynchronizer(accountConfig, eventQueue);
                connectedAccounts.put(accountConfig.getUserId(), synchronizer);
                synchronizer.start();
            } else {
                LOG.warn("Requested to connect to already connected account {}. Will skip it.", accountConfig);
            }
        }
    }

    public void setRetryOn(AccountConfig accountConfig) {
        MatrixSynchronizer synchronizer = connectedAccounts.get(accountConfig.getUserId());
        if (synchronizer == null) {
            LOG.warn("Requested to setup retry for the account {}, but it is not connected. Will skip it.", accountConfig);
            return;
        }
        synchronizer.setRetryOn();
    }

    private void eventCallback() {
        AppEvent event;
        while ((event = eventQueue.pollEvent()) != null) {
            appEventBus.fire(event);
        }
    }

    private void enqueueOutgoingMessage(AppEvent<RoomKey, String> outgoing) {
        RoomKey roomKey = outgoing.getClassifier();
        String messageText = outgoing.getPayload();
        MatrixSynchronizer synchronizer = connectedAccounts.get(roomKey.getUserId());
        if (synchronizer == null) {
            LOG.error("Requested to send outgoing message {}, but synchronizer for userId='{}' not started"
                    , outgoing, roomKey.getUserId());
            return;
        }
        synchronizer.enqueueOutgoingMessage(roomKey.getRoomId(), messageText);
    }

    @PreDestroy
    void shutDown() {
        connectedAccounts.values().forEach(MatrixSynchronizer::disconnect);
    }

}
