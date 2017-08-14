package com.dmytrobilokha.disturber.service.network;


import com.dmytrobilokha.disturber.service.connectionconfig.NetworkConnectionConfigFactory;
import javafx.collections.ObservableList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by dimon on 13.08.17.
 */
@ApplicationScoped
public class MatrixClientService {

    private NetworkConnectionConfigFactory networkConnectionConfigFactory;

    protected MatrixClientService() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    public MatrixClientService(NetworkConnectionConfigFactory networkConnectionConfigFactory) {
        this.networkConnectionConfigFactory = networkConnectionConfigFactory;
    }

    public void connect(ObservableList<String> messageList, String baseUrl) {
        new SynchronizeMessageService(messageList, baseUrl
                , networkConnectionConfigFactory.getNetworkConnectionConfig()).start();
    }

}
