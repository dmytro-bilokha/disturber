package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import com.dmytrobilokha.disturber.network.RoomKey;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Controller for the main layout fxml
 */
@Dependent
public class MainLayoutController {

    private final TreeItem<String> root = new TreeItem<>("ROOT");
    private final AppEventListener<Void, RoomKey> newRoomListener = this::onNewRoom;
    private final MatrixClientService clientService;
    private final AppEventBus appEventBus;

    private ObservableList<String> messageList;

    @FXML
    private TreeView<String> roomsView;
    @FXML
    private ListView<String> messageListView;
    @FXML
    private TextArea messageTyped;

    @Inject
    public MainLayoutController(MatrixClientService clientService, AppEventBus appEventBus) {
        this.clientService = clientService;
        this.appEventBus = appEventBus;
    }

    @FXML
    public void initialize() {
        roomsView.setRoot(root);
        appEventBus.subscribe(this.newRoomListener, AppEvent.empty(AppEvent.Type.MATRIX_NEW_ROOM_SYNCED));
        try {
            clientService.connect();
        } catch (AccountConfigAccessException ex) {
            //TODO add normal error handling
            System.out.println("Failed to get account data:" + ex);
        }
    }

    public void sendButtonHandler() {
        if (messageList != null)
            messageList.add(messageTyped.getText());
        messageTyped.clear();
    }

    public void onNewRoom(AppEvent<Void, RoomKey> appEvent) {
        RoomKey roomKey = appEvent.getPayload();
        TreeItem<String> userIdNode = root.getChildren().stream()
                .filter(node -> node.getValue().equals(roomKey.getUserId()))
                .findAny()
                .orElseGet(() -> attachNewUserId(roomKey.getUserId()));
        userIdNode.getChildren().add(new TreeItem<>(roomKey.getRoomId()));
        roomsView.refresh();
        //TODO migrate to fully messaging
/*        messageList = (ObservableList<String>) clientService.getRoomEventsList(roomKey);
        messageListView.setItems(messageList);
        messageListView.refresh();*/
    }

    private TreeItem<String> attachNewUserId(String userId) {
        TreeItem<String> userIdNode = new TreeItem<>(userId);
        root.getChildren().add(userIdNode);
        return userIdNode;
    }

}
