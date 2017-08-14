package com.dmytrobilokha.disturber.service.connectionconfig;

import com.dmytrobilokha.disturber.service.property.Property;
import com.dmytrobilokha.disturber.service.property.PropertyService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * The factory class responsible for providing network connection configuration
 */
@ApplicationScoped
public class NetworkConnectionConfigFactory {

    private PropertyService propertyService;
    private NetworkConnectionConfig networkConnectionConfig;

    protected NetworkConnectionConfigFactory() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    public NetworkConnectionConfigFactory(PropertyService propertyService) {
        this.propertyService = propertyService;
        networkConnectionConfig = createNetworkConnectionConfig();
    }

    private NetworkConnectionConfig createNetworkConnectionConfig() {
        return new NetworkConnectionConfig(
                propertyService.getInteger(Property.NETWORK_SYNC_TIMEOUT)
                , propertyService.getInteger(Property.NETWORK_SYNC_INTERVAL));
    }

    public NetworkConnectionConfig getNetworkConnectionConfig() {
        return networkConnectionConfig;
    }

}
