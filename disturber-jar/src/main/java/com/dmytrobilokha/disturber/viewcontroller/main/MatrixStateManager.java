package com.dmytrobilokha.disturber.viewcontroller.main;

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
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The class to store and manage Matrix state: events, connected rooms, etc.
 */
@Dependent
public class MatrixStateManager {

    private final AppEventListener<RoomKey, MatrixEvent> newMatrixEventListener = this::storeMatrixEvent;
    private final AppEventListener<String, Void> loginListener = this::handleLogin;
    private final AppEventListener<String, Void> syncListener = this::handleSync;
    private final AppEventListener<AccountConfig, SystemMessage> failListener = this::askForRetryOnFail;

    private final TreeItem<RoomsViewItem> root;
    private final Map<String, Account> accountMap = new HashMap<>();
    private final AppEventBus eventBus;
    private final ViewFactory viewFactory;
    private final MatrixClientService matrixClientService;
    private Consumer<ObservableList<String>> switchChat;

    @Inject
    MatrixStateManager(AppEventBus eventBus, ViewFactory viewFactory, MatrixClientService matrixClientService) {
        this.eventBus = eventBus;
        this.viewFactory = viewFactory;
        this.matrixClientService = matrixClientService;
        this.root = viewFactory.createTreeRoot();
        eventBus.subscribe(newMatrixEventListener, AppEventType.MATRIX_NEW_EVENT_GOT);
        eventBus.subscribe(loginListener, AppEventType.MATRIX_LOGGEDIN);
        eventBus.subscribe(syncListener, AppEventType.MATRIX_SYNCED);
        eventBus.subscribe(failListener, AppEventType.MATRIX_CONNECTION_FAILED);
        eventBus.subscribe(failListener, AppEventType.MATRIX_RESPONSE_FAILED);
    }

    void attachToView(TreeView<RoomsViewItem> view, Consumer<ObservableList<String>> switchChat) {
        this.switchChat = switchChat;
        view.setShowRoot(false);
        view.setEditable(false);
        view.setRoot(root);
    }

    void connect(AccountConfig accountConfig) {
        String userId = accountConfig.getUserId();
        Account account = accountMap.get(userId);
        if (account == null) {
            addNewAccount(accountConfig);
            return;
        }
        if (account.getState() == AccountState.STOPPED) {
            retryAccountConnect(account);
            return;
        }
    }

    private Account addNewAccount(AccountConfig accountConfig) {
        Account account = new Account(accountConfig, viewFactory, root, switchChat);
        accountMap.put(accountConfig.getUserId(), account);
        account.setState(AccountState.CONNECTING);
        matrixClientService.connect(accountConfig);
        return account;
    }

    private void retryAccountConnect(Account account) {
        account.setState(AccountState.CONNECTING);
        matrixClientService.setRetryOn(account.getAccountConfig());
    }

    private void handleLogin(AppEvent<String, Void> loginEvent) {
        accountMap.get(loginEvent.getClassifier())
                .reset()
                .setState(AccountState.CONNECTED);
    }

    private void handleSync(AppEvent<String, Void> syncEvent) {
        accountMap.get(syncEvent.getClassifier())
                .setState(AccountState.CONNECTED);
    }

    private void storeMatrixEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        accountMap.get(appEvent.getClassifier().getUserId())
                .onEvent(appEvent.getClassifier(), appEvent.getPayload());
    }

    private void askForRetryOnFail(AppEvent<AccountConfig, SystemMessage> failEvent) {
        SystemMessage failMessage = failEvent.getPayload();
        Account account = accountMap.get(failEvent.getClassifier().getUserId())
                .setState(AccountState.STOPPED);
        DialogButton userChoice = viewFactory.showErrorDialog(failMessage, DialogButton.RETRY, DialogButton.STOP);
        if (userChoice == DialogButton.RETRY) {
            retryAccountConnect(account);
        }
    }

}
