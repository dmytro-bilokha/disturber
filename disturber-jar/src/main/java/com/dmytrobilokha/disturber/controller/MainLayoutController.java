package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import com.dmytrobilokha.disturber.network.MatrixEvent;
import com.dmytrobilokha.disturber.network.RoomKey;
import com.dmytrobilokha.disturber.view.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Optional;

/**
 * Controller for the main layout fxml
 */
@Dependent
public class MainLayoutController {

    private final TreeItem<String> root = new TreeItem<>("ROOT");
    private final AppEventListener<RoomKey, MatrixEvent> newRoomEventListener = this::onNewRoomEvent;
    private final MatrixClientService clientService;
    private final AppEventBus appEventBus;
    private final ViewFactory viewFactory;

    @FXML
    private TreeView<String> roomsView;
    @FXML
    private TabPane chatTabPane;
    @FXML
    private TextArea messageTyped;

    @Inject
    public MainLayoutController(MatrixClientService clientService, AppEventBus appEventBus, ViewFactory viewFactory) {
        this.clientService = clientService;
        this.appEventBus = appEventBus;
        this.viewFactory = viewFactory;
    }

    @FXML
    public void initialize() {
        roomsView.setRoot(root);
        appEventBus.subscribe(this.newRoomEventListener, AppEventType.MATRIX_NEW_EVENT_GOT);
        try {
            clientService.connect();
        } catch (AccountConfigAccessException ex) {
            //TODO add normal error handling
            System.out.println("Failed to get account data:" + ex);
        }
    }

    public void sendButtonHandler() {
        messageTyped.clear();
    }

    private void onNewRoomEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        RoomKey roomKey = appEvent.getClassifier();
        TreeItem<String> userIdNode = root.getChildren().stream()
                .filter(node -> node.getValue().equals(roomKey.getUserId()))
                .findAny()
                .orElseGet(() -> attachNewUserId(roomKey.getUserId()));
        Optional<TreeItem<String>> roomIdNode = userIdNode.getChildren().stream()
                .filter(node -> node.getValue().equals(roomKey.getRoomId()))
                .findAny();
        if (!roomIdNode.isPresent()) {
            attachNewRoom(userIdNode, roomKey);
        }
    }

    private TreeItem<String> attachNewUserId(String userId) {
        TreeItem<String> userIdNode = new TreeItem<>(userId);
        root.getChildren().add(userIdNode);
        return userIdNode;
    }

    private void attachNewRoom(TreeItem<String> userIdNode, RoomKey roomKey) {
        userIdNode.getChildren().add(new TreeItem<>(roomKey.getRoomId()));
        Tab newRoomTab = viewFactory.produceChatTab(roomKey);
        chatTabPane.getTabs().add(newRoomTab);
    }

}
