package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.SystemMessage;
import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.viewcontroller.DialogButton;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
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

    private final AppEventListener<RoomKey, MatrixEvent> newMatrixEventListener = this::handleMessageEvent;
    private final AppEventListener<RoomKey, MatrixEvent> inviteEventListener = this::handleInviteEvent;
    private final AppEventListener<RoomKey, Void> joinedEventListener = this::handleJoinedEvent;
    private final AppEventListener<String, Void> loginListener = this::handleLogin;
    private final AppEventListener<String, Void> syncListener = this::handleSync;
    private final AppEventListener<AccountConfig, SystemMessage> failListener = this::askForRetryOnFail;
    private final AppEventListener<AccountConfig, SystemMessage> issueListener = this::notifyOnIssue;

    private final TreeItem<RoomsViewItem> root;
    private final Map<String, Account> accountMap = new HashMap<>();
    private final AppEventBus eventBus;
    private final ViewFactory viewFactory;
    private Consumer<Room> switchChat;
    private Runnable notifier;

    @Inject
    MatrixStateManager(AppEventBus eventBus, ViewFactory viewFactory) {
        this.eventBus = eventBus;
        this.viewFactory = viewFactory;
        this.root = viewFactory.createTreeRoot();
        eventBus.subscribe(newMatrixEventListener, AppEventType.MATRIX_NEW_EVENT_GOT);
        eventBus.subscribe(inviteEventListener, AppEventType.MATRIX_NEW_INVITE_GOT);
        eventBus.subscribe(joinedEventListener, AppEventType.MATRIX_JOINED_OK);
        eventBus.subscribe(loginListener, AppEventType.MATRIX_LOGGEDIN);
        eventBus.subscribe(syncListener, AppEventType.MATRIX_SYNCED);
        eventBus.subscribe(failListener, AppEventType.MATRIX_CONNECTION_FAILED);
        eventBus.subscribe(issueListener, AppEventType.MATRIX_CONNECTION_ISSUE);
        eventBus.subscribe(failListener, AppEventType.MATRIX_RESPONSE_FAILED);
    }

    void attachToView(TreeView<RoomsViewItem> view, Consumer<Room> switchChat, Runnable notifier) {
        this.switchChat = switchChat;
        this.notifier = notifier;
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

    void sendMessage(RoomKey roomKey, String message) {
        eventBus.fire(AppEvent.withClassifierAndPayload(AppEventType.MATRIX_OUTGOING_MESSAGE, roomKey, message));
    }

    private Account addNewAccount(AccountConfig accountConfig) {
        Account account = new Account(accountConfig, viewFactory, root, switchChat, this::requestJoin);
        accountMap.put(accountConfig.getUserId(), account);
        account.setState(AccountState.CONNECTING);
        eventBus.fire(AppEvent.withPayload(AppEventType.MATRIX_CMD_CONNECT, accountConfig));
        return account;
    }

    private void retryAccountConnect(Account account) {
        account.setState(AccountState.CONNECTING);
        eventBus.fire(AppEvent.withPayload(AppEventType.MATRIX_CMD_RETRY, account.getAccountConfig()));
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

    private void handleMessageEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        accountMap.get(appEvent.getClassifier().getUserId())
                .onEvent(appEvent.getClassifier(), appEvent.getPayload());
        notifier.run();
    }

    private void handleJoinedEvent(AppEvent<RoomKey, Void> appEvent) {
        accountMap.get(appEvent.getClassifier().getUserId())
                .onJoined(appEvent.getClassifier());
    }

    private void requestJoin(RoomKey roomKey) {
        eventBus.fire(AppEvent.withClassifier(AppEventType.MATRIX_JOIN, roomKey));
    }

    private void handleInviteEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        accountMap.get(appEvent.getClassifier().getUserId())
                .onInvite(appEvent.getClassifier(), appEvent.getPayload());
        notifier.run();
    }

    private void notifyOnIssue(AppEvent<AccountConfig, SystemMessage> issueEvent) {
        accountMap.get(issueEvent.getClassifier().getUserId())
                .setState(AccountState.CONNECTING);
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
