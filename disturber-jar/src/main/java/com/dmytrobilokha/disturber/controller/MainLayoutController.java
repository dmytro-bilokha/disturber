package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import com.dmytrobilokha.disturber.network.MatrixEvent;
import com.dmytrobilokha.disturber.network.RoomKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
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

    private final ObservableList<String> messageList = FXCollections.observableArrayList();

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
        messageListView.setItems(messageList);
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
        if (messageList != null)
            messageList.add(messageTyped.getText());
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
        if (!roomIdNode.isPresent())
            userIdNode.getChildren().add(new TreeItem<>(roomKey.getRoomId()));
        roomsView.refresh();
        MatrixEvent matrixEvent = appEvent.getPayload();
        messageList.add(matrixEvent.toString());
        messageListView.refresh();
    }

    private TreeItem<String> attachNewUserId(String userId) {
        TreeItem<String> userIdNode = new TreeItem<>(userId);
        root.getChildren().add(userIdNode);
        return userIdNode;
    }

}
