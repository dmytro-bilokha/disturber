package com.dmytrobilokha.disturber.service.network;


import javafx.collections.ObservableList;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by dimon on 13.08.17.
 */
@ApplicationScoped
public class MatrixClientService {

    public void connect(ObservableList<String> messageList, String baseUrl) {
        new SynchronizeMessageService(messageList, baseUrl).start();
    }

}
