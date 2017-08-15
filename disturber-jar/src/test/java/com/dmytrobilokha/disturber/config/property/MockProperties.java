package com.dmytrobilokha.disturber.config.property;

import static com.dmytrobilokha.disturber.Constants.NEW_LINE;

/**
 * Class with mock properties sets. It emulates the properties file on the filesystem
 */
public class MockProperties {


    public static final String MINIMAL_PROPERTIES = "network.sync.timeout=1" + NEW_LINE
            + "network.sync.interval=2" + NEW_LINE
            + "properties.version=0";

    public static final String MISSING_MANDATORY = "network.sync.timeout=1" + NEW_LINE
            + "network.sync.interval=2" + NEW_LINE;

    public static final String INVALID_VERSION = "network.sync.timeout=1" + NEW_LINE
            + "network.sync.interval=2" + NEW_LINE
            + "properties.version=here we expect number";

    private MockProperties() {
        //No need to instantiate
    }
}
