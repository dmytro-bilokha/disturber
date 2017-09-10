package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import com.dmytrobilokha.disturber.network.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Controller for the main layout fxml
 */
@Dependent
public class MainLayoutController {

    private final TreeItem<RoomKey> root = new TreeItem<>();
    private final AppEventListener<RoomKey, MatrixEvent> newRoomEventListener = this::onMatrixEvent;
    private final MatrixClientService clientService;
    private final AppEventBus appEventBus;
    private final ViewFactory viewFactory;

    @FXML
    private TreeView<RoomKey> roomsView;
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
        roomsView.setCellFactory(view -> {
            TreeCell<RoomKey> cell = new TreeCell<RoomKey>(){
                @Override
                protected void updateItem(RoomKey item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.hasRoomId() ? item.getRoomId() : item.getUserId());
                    }
                }
            };
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && cell.getItem() != null && cell.getItem().hasRoomId())
                    openChatTab(cell.getItem());
            } );
            cell.setEditable(false);
            return cell;
        });
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

    private void onMatrixEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        TreeItem<RoomKey> userIdNode = attachUserIdIfNew(appEvent.getClassifier().getUserId());
        attachRoomIfNew(userIdNode, appEvent.getClassifier());
    }

    private TreeItem<RoomKey> attachUserIdIfNew(String userId) {
        RoomKey userIdKey = new RoomKey(userId);
        return findOrCreateItemByRoomKey(root, userIdKey);
    }

    private TreeItem<RoomKey> findOrCreateItemByRoomKey(TreeItem<RoomKey> parent, RoomKey searchedKey) {
        return  parent.getChildren().stream()
                .filter(item -> item.getValue().equals(searchedKey))
                .findAny().orElseGet(() -> createAndAttachItem(parent, searchedKey));
    }

    private TreeItem<RoomKey> createAndAttachItem(TreeItem<RoomKey> parent, RoomKey roomKey) {
        TreeItem<RoomKey> newItem = new TreeItem<>(roomKey);
        parent.getChildren().add(newItem);
        return newItem;
    }

    private TreeItem<RoomKey> attachRoomIfNew(TreeItem<RoomKey> userIdNode, RoomKey roomKey) {
        return findOrCreateItemByRoomKey(userIdNode, roomKey);
    }

    private void openChatTab(RoomKey roomKey) {
        Tab newRoomTab = viewFactory.produceChatTab(roomKey);
        chatTabPane.getTabs().add(newRoomTab);
    }

}
