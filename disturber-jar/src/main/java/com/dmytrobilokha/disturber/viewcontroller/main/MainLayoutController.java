package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.MatrixStateService;
import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.config.account.AccountConfigService;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the main layout fxml
 */
//TODO: implement different style for active room to distinguish it.
@Dependent
public class MainLayoutController {

    private final TreeItem<RoomKey> root = new TreeItem<>();
    private final ObservableList<String> messageList = FXCollections.observableArrayList();
    private final AppEventListener<RoomKey, MatrixEvent> newRoomEventListener = this::onMatrixEvent;
    private final MatrixClientService clientService;
    private final AppEventBus appEventBus;
    private final MatrixStateService historyKeeper;
    private final AccountConfigService accountService;
    private final ViewFactory viewFactory;

    private RoomKey currentRoom;

    @FXML
    private TreeView<RoomKey> roomsView;
    @FXML
    private ListView<String> messageListView;
    @FXML
    private TextArea messageTyped;

    @Inject
    public MainLayoutController(MatrixClientService clientService, AppEventBus appEventBus
            , MatrixStateService historyKeeper, AccountConfigService accountService, ViewFactory viewFactory) {
        this.clientService = clientService;
        this.appEventBus = appEventBus;
        this.historyKeeper = historyKeeper;
        this.accountService = accountService;
        this.viewFactory = viewFactory;
    }

    @FXML
    public void initialize() {
        messageListView.setItems(messageList);
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
                    switchActiveChat(cell.getItem());
            } );
            cell.setEditable(false);
            return cell;
        });
        appEventBus.subscribe(this.newRoomEventListener, AppEventType.MATRIX_NEW_EVENT_GOT);
        List<AccountConfig>  accounts;
        try {
            accounts = accountService.getAccountConfigs();
        } catch (AccountConfigAccessException ex) {
            viewFactory.showErrorAlert(ex.getSystemMessage());
            return;
        }
        clientService.connect(accounts);
    }

    public void sendButtonHandler() {
        String text = messageTyped.getText();
        if (currentRoom == null || text.isEmpty())
            return;
        appEventBus.fire(AppEvent
                .withClassifierAndPayload(AppEventType.MATRIX_OUTGOING_MESSAGE, currentRoom, text));
        messageTyped.clear();
    }

    private void onMatrixEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        handleNewRoom(appEvent);
        updateChat(appEvent);
    }

    private void handleNewRoom(AppEvent<RoomKey, MatrixEvent> appEvent) {
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

    private void updateChat(AppEvent<RoomKey, MatrixEvent> appEvent) {
        if (!Objects.equals(currentRoom, appEvent.getClassifier()))
            return;
        messageList.add(appEvent.getPayload().toString());
    }

    private void switchActiveChat(RoomKey roomKey) {
        if (Objects.equals(currentRoom, roomKey))
            return;
        messageList.clear();
        currentRoom = roomKey;
        historyKeeper.getRoomEventsHistory(roomKey).stream()
                .map(MatrixEvent::toString)
                .forEach(messageList::add);
        messageListView.refresh();
    }

}
