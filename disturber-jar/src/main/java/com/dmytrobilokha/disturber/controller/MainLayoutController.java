package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import com.dmytrobilokha.disturber.network.NewRoomHandler;
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
public class MainLayoutController implements NewRoomHandler {

    private final TreeItem<String> root = new TreeItem<>("ROOT");
    private final MatrixClientService clientService;

    private ObservableList<String> messageList;

    @FXML
    private TreeView<String> roomsView;
    @FXML
    private ListView<String> messageListView;
    @FXML
    private TextArea messageTyped;

    @Inject
    public MainLayoutController(MatrixClientService clientService) {
        this.clientService = clientService;
    }

    @FXML
    public void initialize() {
        roomsView.setRoot(root);
        clientService.setNewRoomHandler(this);
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

    @Override
    public void onNewRoom(RoomKey roomKey) {
        TreeItem<String> userIdNode = root.getChildren().stream()
                .filter(node -> node.getValue().equals(roomKey.getUserId()))
                .findAny()
                .orElseGet(() -> attachNewUserId(roomKey.getUserId()));
        userIdNode.getChildren().add(new TreeItem<>(roomKey.getRoomId()));
        roomsView.refresh();
        messageList = (ObservableList<String>) clientService.getRoomEventsList(roomKey);
        messageListView.setItems(messageList);
        messageListView.refresh();
    }

    private TreeItem<String> attachNewUserId(String userId) {
        TreeItem<String> userIdNode = new TreeItem<>(userId);
        root.getChildren().add(userIdNode);
        return userIdNode;
    }

}
