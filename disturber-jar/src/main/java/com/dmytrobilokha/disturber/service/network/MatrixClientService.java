package com.dmytrobilokha.disturber.service.network;


import com.dmytrobilokha.disturber.service.property.Property;
import com.dmytrobilokha.disturber.service.property.PropertyService;
import javafx.collections.ObservableList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by dimon on 13.08.17.
 */
@ApplicationScoped
public class MatrixClientService {

    @Inject
    private PropertyService propertyService;

    public void connect(ObservableList<String> messageList, String baseUrl) {
        new SynchronizeMessageService(messageList, baseUrl, getNetworkConnectionConfig()).start();
    }

    private NetworkConnectionConfig getNetworkConnectionConfig() {
       return new NetworkConnectionConfig(
               propertyService.getInteger(Property.NETWORK_SYNC_TIMEOUT)
               , propertyService.getInteger(Property.NETWORK_SYNC_INTERVAL));
    }

}
