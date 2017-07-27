package com.dmytrobilokha.disturber.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private int messageNumber = 0;

    public MessageService() {
        LOG.info("MessageService constructor called");
    }

    public String getMessage() {
        return "Message number " + messageNumber++;
    }
}
