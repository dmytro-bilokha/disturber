package com.dmytrobilokha.disturber.network;


import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.connection.NetworkConnectionConfigFactory;
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

    public void connect(ObservableList<String> messageList, AccountConfig accountConfig) {
        MatrixAccount account = new MatrixAccount(accountConfig, networkConnectionConfigFactory.getNetworkConnectionConfig());
        new SynchronizeMessageService(messageList, account).start();
    }

}
