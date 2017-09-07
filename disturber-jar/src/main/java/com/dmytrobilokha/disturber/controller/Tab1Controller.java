package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.service.MessageService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class Tab1Controller {

    private static final Logger LOG = LoggerFactory.getLogger(Tab1Controller.class);

    @FXML
    private Label nameLabel;
    @FXML
    private TextField name;

    private MessageService messageService;
    private AppEventBus eventBus;

    @Inject
    public Tab1Controller(MessageService messageService, AppEventBus eventBus) {
        LOG.info("Tab1Controller constructor called. And message is '{}'", messageService.getMessage());
        this.messageService = messageService;
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        LOG.info("PostConstruct called");
    }

    public void submitHandler(ActionEvent event) {
        String nameSubmitted = name.getText();
        LOG.info("Submitted name '{}'", nameSubmitted);
        LOG.debug("Notified listeners");
    }

}
