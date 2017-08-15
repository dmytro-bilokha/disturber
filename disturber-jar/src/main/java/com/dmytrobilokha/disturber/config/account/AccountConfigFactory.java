package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.config.property.PropertyService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;

/**
 * The factory responsible for providing accounts configuration
 */
@ApplicationScoped
public class AccountConfigFactory {

    private Path configDirPath;

    protected AccountConfigFactory() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    public AccountConfigFactory(PropertyService propertyService) {
    }
}
