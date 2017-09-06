package com.dmytrobilokha.disturber.network;


import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.config.account.AccountConfigFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The service responsible for keeping in sync with Matrix accounts. It aggregates UI-related stuff with
 * Matrix API stuff.
 */
@ApplicationScoped
public class MatrixClientService {

    private final Map<AccountConfig, MatrixSynchronizer> connectedAccounts = new HashMap<>();
    private final Map<RoomKey, List<String>> roomEventMap = new HashMap<>();

    private MatrixEventQueue eventQueue;

    private AccountConfigFactory accountConfigFactory;
    private AppEventBus appEventBus;

    protected MatrixClientService() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    protected MatrixClientService(AccountConfigFactory accountConfigFactory, AppEventBus appEventBus
            , RunLaterWrapper runLaterWrapper) {
        this.accountConfigFactory = accountConfigFactory;
        this.appEventBus = appEventBus;
        this.eventQueue = new MatrixEventQueue(runLaterWrapper.wrap(this::eventCallback));
    }

    public void connect() throws AccountConfigAccessException {
        List<AccountConfig> configs = accountConfigFactory.getAccountConfigs();
        for (AccountConfig accountConfig : configs) {
            if (!connectedAccounts.containsKey(accountConfig)) {
                MatrixSynchronizer synchronizer = new MatrixSynchronizer(accountConfig, eventQueue, new MatrixApiConnector());
                connectedAccounts.put(accountConfig, synchronizer);
                synchronizer.start();
            }
        }
    }

    public Map<String, Set<String>> getRoomsStructure() {
        return roomEventMap.keySet().stream()
                .collect(Collectors
                        .groupingBy(RoomKey::getUserId
                                , Collectors.mapping(RoomKey::getRoomId, Collectors.toSet())));
    }

    public List<String> getRoomEventsList(RoomKey roomKey) {
        return roomEventMap.get(roomKey);
    }

    private void eventCallback() {
        MatrixEvent event;
        while ((event = eventQueue.pollEvent()) != null) {
            RoomKey roomKey = event.getRoomKey();
            if (!roomEventMap.containsKey(roomKey)) {
                roomEventMap.put(roomKey, new ArrayList<>());
                appEventBus.fire(AppEvent.of(AppEvent.Type.MATRIX_NEW_ROOM_SYNCED, roomKey));
            }
            roomEventMap.get(roomKey).add(event.toString());
        }
    }

    @PreDestroy
    public void shutDown() {
        connectedAccounts.values().forEach(MatrixSynchronizer::disconnect);
    }

}
