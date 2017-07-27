package com.dmytrobilokha.disturber.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MessageServiceTest {

    private MessageService messageService;

    @Before
    public void initService() {
        messageService = new MessageService();
    }

    @Test
    public void testInitialMessage() {
        assertEquals("Message number 0", messageService.getMessage());
    }

    @Test
    public void testSecondMessage() {
        messageService.getMessage();
        assertEquals("Message number 1", messageService.getMessage());
    }

}
