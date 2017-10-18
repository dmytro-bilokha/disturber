package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.config.account.AccountConfigService;
import com.dmytrobilokha.disturber.viewcontroller.AppIcon;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
/**
 * Controller for the main layout fxml
 */
//TODO: implement different style for active room to distinguish it.
@Dependent
public class MainLayoutController {

    private final MatrixStateManager matrixStateManager;
    private final AccountConfigService accountService;
    private final ViewFactory viewFactory;

    private Stage mainStage;
    private boolean focused;
    private AppIcon currentIcon;
    private Image quietIcon;
    private Image gotMessageIcon;
    private RoomKey currentRoomKey;

    @FXML
    private TreeView<RoomsViewItem> roomsView;
    @FXML
    private ListView<TextField> messageListView;
    @FXML
    private TextArea messageTyped;

    @Inject
    public MainLayoutController(MatrixStateManager matrixStateManager, AccountConfigService accountService
            , ViewFactory viewFactory) {
        this.matrixStateManager = matrixStateManager;
        this.accountService = accountService;
        this.viewFactory = viewFactory;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
        quietIcon = viewFactory.createIcon(AppIcon.MAIN_QUIET);
        gotMessageIcon = viewFactory.createIcon(AppIcon.MAIN_MESSAGE);
        setQuietIcon();
        mainStage.focusedProperty().addListener(this::focusListener);
        messageTyped.setOnKeyReleased(this::handleTextAreaKeyEvent);
        roomsView.setCellFactory(new RoomsViewCellFactory());
        List<AccountConfig>  accounts;
        try {
            accounts = accountService.getAccountConfigs();
        } catch (AccountConfigAccessException ex) {
            viewFactory.showErrorAlert(ex.getSystemMessage());
            return;
        }
        matrixStateManager.attachToView(roomsView, this::switchActiveChat, this::flagMessage);
        accounts.forEach(matrixStateManager::connect);
    }

    private void setQuietIcon() {
        mainStage.getIcons().clear();
        mainStage.getIcons().add(quietIcon);
        currentIcon = AppIcon.MAIN_QUIET;
    }

    private void setGotMessageIcon() {
        mainStage.getIcons().clear();
        mainStage.getIcons().add(gotMessageIcon);
        currentIcon = AppIcon.MAIN_MESSAGE;
    }

    public void sendButtonHandler() {
        if (currentRoomKey == null || messageTyped.getText().isEmpty())
            return;
        matrixStateManager.sendMessage(currentRoomKey, messageTyped.getText());
        messageTyped.clear();
    }

    private void switchActiveChat(Room room) {
        messageListView.setItems(room.getEventsList());
        currentRoomKey = room.getRoomKey();
    }

    private void focusListener(ObservableValue<? extends Boolean> value, Boolean oldPropertyValue
            , Boolean newPropertyValue) {
        focused = newPropertyValue;
        if (focused && currentIcon == AppIcon.MAIN_MESSAGE)
            setQuietIcon();
    }

    private void flagMessage() {
        if (!focused && currentIcon == AppIcon.MAIN_QUIET) {
            setGotMessageIcon();
            mainStage.toFront();
        }
    }

    private void handleTextAreaKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.isControlDown())
            sendButtonHandler();
    }

}
