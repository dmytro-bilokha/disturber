package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.config.account.AccountConfigService;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;

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

    @FXML
    private TreeView<RoomsViewItem> roomsView;
    @FXML
    private ListView<String> messageListView;
    @FXML
    private TextArea messageTyped;

    @Inject
    public MainLayoutController(MatrixStateManager matrixStateManager, AccountConfigService accountService, ViewFactory viewFactory) {
        this.matrixStateManager = matrixStateManager;
        this.accountService = accountService;
        this.viewFactory = viewFactory;
    }

    @FXML
    public void initialize() {
        roomsView.setCellFactory(new RoomsViewCellFactory());
        List<AccountConfig>  accounts;
        try {
            accounts = accountService.getAccountConfigs();
        } catch (AccountConfigAccessException ex) {
            viewFactory.showErrorAlert(ex.getSystemMessage());
            return;
        }
        matrixStateManager.attachToView(roomsView, this::switchActiveChat);
        accounts.forEach(matrixStateManager::connect);
    }

    public void sendButtonHandler() {
        return;
    }

    private void switchActiveChat(ObservableList<String> eventsList) {
        messageListView.setItems(eventsList);
    }

}
