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
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
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
    private final AppEventListener<RoomKey, Void> joinRequestListener = this::enqueueJoinRequest;
    private final AppEventListener<Void, AccountConfig> connectCommandListener = this::connect;
    private final AppEventListener<Void, AccountConfig> retryCommandListener = this::setRetryOn;

    private CrossThreadEventQueue eventQueue;

    private AppEventBus appEventBus;
    private MatrixSynchronizerFactory synchronizerFactory;

    protected MatrixClientService() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    MatrixClientService(AppEventBus appEventBus, MatrixSynchronizerFactory synchronizerFactory) {
        this.appEventBus = appEventBus;
        this.synchronizerFactory = synchronizerFactory;
        this.eventQueue = synchronizerFactory.createCrossThreadEventQueue(this::eventCallback);
        appEventBus.subscribe(outgoingMessageListener, AppEventType.MATRIX_OUTGOING_MESSAGE);
        appEventBus.subscribe(joinRequestListener, AppEventType.MATRIX_JOIN);
        appEventBus.subscribe(connectCommandListener, AppEventType.MATRIX_CMD_CONNECT);
        appEventBus.subscribe(retryCommandListener, AppEventType.MATRIX_CMD_RETRY);
    }

    private void connect(AppEvent<Void, AccountConfig> connectEvent) {
        AccountConfig accountConfig = connectEvent.getPayload();
        if (!connectedAccounts.containsKey(accountConfig.getUserId())) {
            MatrixSynchronizer synchronizer = synchronizerFactory.createMatrixSynchronizer(accountConfig, eventQueue);
            connectedAccounts.put(accountConfig.getUserId(), synchronizer);
            synchronizer.start();
        } else {
            LOG.warn("Requested to connect to already connected account {}. Will skip it.", accountConfig);
        }
    }

    private void setRetryOn(AppEvent<Void, AccountConfig> retryEvent) {
        AccountConfig accountConfig = retryEvent.getPayload();
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

    private void enqueueJoinRequest(AppEvent<RoomKey, Void> joinEvent) {
        RoomKey roomKey = joinEvent.getClassifier();
        MatrixSynchronizer synchronizer = connectedAccounts.get(roomKey.getUserId());
        if (synchronizer == null) {
            LOG.error("Requested to send join request {}, but synchronizer for userId='{}' not started"
                    , joinEvent, roomKey.getUserId());
            return;
        }
        synchronizer.enqueueJoin(roomKey.getRoomId());
    }

    @PreDestroy
    void shutDown() {
        connectedAccounts.values().forEach(MatrixSynchronizer::disconnect);
    }

    public void eagerInit(@Observes @Initialized(ApplicationScoped.class) Object initEvent) {
        //The methods does nothing. We need it just to ensure a CDI framework initializes the bean eagerly.
        //Because this bean is not used directly (only via messaging) without this method it won't be initialized.
    }

}
