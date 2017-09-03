package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.config.account.AccountConfigAccessException;
import com.dmytrobilokha.disturber.config.account.AccountConfigFactory;
import com.dmytrobilokha.disturber.network.MatrixClientService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Controller for the main layout fxml
 */
@Dependent
public class MainLayoutController {

    private static final Logger LOG = LoggerFactory.getLogger(MainLayoutController.class);
    private final MatrixClientService clientService;
    private final AccountConfigFactory accountConfigFactory;

    private ObservableList<String> messageList;

    @FXML
    private ListView<String> messageListView;
    @FXML
    private TextArea messageTyped;

    @Inject
    public MainLayoutController(MatrixClientService clientService, AccountConfigFactory accountConfigFactory) {
        this.clientService = clientService;
        this.accountConfigFactory = accountConfigFactory;
    }

    @FXML
    public void initialize() {
        try {
            messageList = clientService.connect(accountConfigFactory.getAccountConfigs().get(0));
        } catch (AccountConfigAccessException ex) {
            //TODO add normal error handling
            System.out.println("Failed to get account data:" + ex);
        }
        messageListView.setItems(messageList);
        messageListView.refresh();
    }

    public void sendButtonHandler() {
        if (messageList != null)
            messageList.add(messageTyped.getText());
        messageTyped.clear();
    }
}
