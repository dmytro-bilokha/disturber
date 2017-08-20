package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.network.MatrixClientService;
import javafx.collections.FXCollections;
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

    private final ObservableList<String> messageList = FXCollections.observableArrayList("Test1", "Test2", "Test3");

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
        messageListView.setItems(messageList);
        messageListView.refresh();
        clientService.connect(messageList, "https://gturnquist-quoters.cfapps.io/api/");
    }

    public void sendButtonHandler() {
        messageList.add(messageTyped.getText());
        messageTyped.clear();
    }
}
