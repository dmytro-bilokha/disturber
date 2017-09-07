package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.service.MessageService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class Tab2Controller {

    private static final Logger LOG = LoggerFactory.getLogger(Tab2Controller.class);

    @FXML
    private Label nameValueLabel;

    private MessageService messageService;
    private AppEventBus eventBus;

    @Inject
    public Tab2Controller(MessageService messageService, AppEventBus eventBus) {
        LOG.info("Tab2Controller constructor called. And message is '{}'", messageService.getMessage());
        this.messageService = messageService;
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void shutDown() {
        LOG.debug("PreDestroy called and message is '{}'", messageService.getMessage()); //It never happens
    }

    public void uselessHandler(ActionEvent event) {
        LOG.debug("Got event {}", event);
    }

    public void onAppEvent(AppEvent appEvent) {
        nameValueLabel.setText("Hello, " + appEvent.getPayload());
    }
}
