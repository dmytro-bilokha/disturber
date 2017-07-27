package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class TabPanelController {

    private static final Logger LOG = LoggerFactory.getLogger(Tab2Controller.class);

    @Inject
    public TabPanelController(MessageService messageService) {
        LOG.info("TabPanelController constructor called. And message is '{}'", messageService.getMessage());
    }

    @PostConstruct
    public void init() {
        LOG.info("PostConstruct called");
    }

    @PreDestroy
    public void cleanUp() {
        LOG.info("PreDestroy called");
    }
}
