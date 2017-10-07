package com.dmytrobilokha.disturber.chatstate;

import com.dmytrobilokha.disturber.SystemMessage;
import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import com.dmytrobilokha.disturber.viewcontroller.DialogButton;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import com.dmytrobilokha.disturber.viewcontroller.main.RoomsView;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The service to store and manage Matrix state: events, connected rooms, etc.
 */
@ApplicationScoped
public class MatrixStateService {

    private final Map<RoomKey, List<MatrixEvent>> roomEventMap = new HashMap<>();
    private final Map<String, AccountState> accountStateMap = new HashMap<>();

    private final AppEventListener<RoomKey, MatrixEvent> newMatrixEventListener = this::storeMatrixEvent;
    private final AppEventListener<String, Void> loginListener = this::handleLogin;
    private final AppEventListener<String, Void> syncListener = this::handleSync;
    private final AppEventListener<AccountConfig, SystemMessage> failListener = this::askForRetryOnFail;

    private ViewFactory viewFactory;
    private MatrixClientService matrixClientService;
    private RoomsView roomsView;

    protected MatrixStateService() {
        //No args constructor to keep CDI framework happy
    }

    @Inject
    public MatrixStateService(AppEventBus eventBus, ViewFactory viewFactory, MatrixClientService matrixClientService) {
        this.viewFactory = viewFactory;
        this.matrixClientService = matrixClientService;
        eventBus.subscribe(newMatrixEventListener, AppEventType.MATRIX_NEW_EVENT_GOT);
        eventBus.subscribe(loginListener, AppEventType.MATRIX_LOGGEDIN);
        eventBus.subscribe(syncListener, AppEventType.MATRIX_SYNCED);
        eventBus.subscribe(failListener, AppEventType.MATRIX_CONNECTION_FAILED);
        eventBus.subscribe(failListener, AppEventType.MATRIX_RESPONSE_FAILED);
    }

    public void setRoomsView(RoomsView roomsView) {
        this.roomsView = roomsView;
    }

    public void connect(AccountConfig accountConfig) {
        if (roomsView == null)
            throw new IllegalStateException("First RoomsView should be set to connect");
        AccountState state = accountStateMap.get(accountConfig.getUserId());
        if (state == null) {
            roomsView.addAccount(accountConfig.getUserId());
            changeAccountState(accountConfig.getUserId(), AccountState.CONNECTING);
            matrixClientService.connect(accountConfig);
            return;
        }
        if (state == AccountState.STOPPED) { //May be this should not be here, but in the separate method
            changeAccountState(accountConfig.getUserId(), AccountState.CONNECTING);
            matrixClientService.setRetryOn(accountConfig);
            return;
        }
    }

    private void changeAccountState(String userId, AccountState newState) {
        if (accountStateMap.get(userId) == newState)
            return;
        accountStateMap.put(userId, newState);
        roomsView.changeAccountState(userId, newState);
    }

    public List<MatrixEvent> getRoomEventsHistory(RoomKey roomKey) {
        List<MatrixEvent> roomEventsHistory = roomEventMap.get(roomKey);
        if (roomEventsHistory == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(roomEventsHistory);
    }

    private void handleLogin(AppEvent<String, Void> loginEvent) {
        String userId = loginEvent.getClassifier();
        changeAccountState(userId, AccountState.CONNECTED);
        roomsView.resetAccount(userId);
        for (Iterator<RoomKey> keyIterator = roomEventMap.keySet().iterator(); keyIterator.hasNext();) {
            RoomKey roomKey = keyIterator.next();
            if (userId.equals(roomKey.getUserId()))
                keyIterator.remove();
        }
    }

    private void handleSync(AppEvent<String, Void> syncEvent) {
        changeAccountState(syncEvent.getClassifier(), AccountState.CONNECTED);
    }

    private void storeMatrixEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        RoomKey roomKey = appEvent.getClassifier();
        List<MatrixEvent> events = roomEventMap.get(roomKey);
        if (events == null) {
            events = addNewRoom(roomKey);
        }
        events.add(appEvent.getPayload());
        roomsView.onEvent(roomKey, appEvent.getPayload());
    }

    private List<MatrixEvent> addNewRoom(RoomKey roomKey) {
        List<MatrixEvent> events = new ArrayList<>();
        roomEventMap.put(roomKey, events);
        roomsView.addNewRoom(roomKey);
        return events;
    }

    private void askForRetryOnFail(AppEvent<AccountConfig, SystemMessage> failEvent) {
        SystemMessage failMessage = failEvent.getPayload();
        changeAccountState(failEvent.getClassifier().getUserId(), AccountState.STOPPED);
        DialogButton userChoice = viewFactory.showErrorDialog(failMessage, DialogButton.RETRY, DialogButton.STOP);
        if (userChoice == DialogButton.RETRY) {
            changeAccountState(failEvent.getClassifier().getUserId(), AccountState.CONNECTING);
            matrixClientService.setRetryOn(failEvent.getClassifier());
        }
    }

    public void eagerInit(@Observes @Initialized(ApplicationScoped.class) Object initEvent) {
        //The methods does nothing. We need it just to ensure a CDI framework initializes the bean eagerly, so we won't
        //miss Matrix events in our history.
    }

}
