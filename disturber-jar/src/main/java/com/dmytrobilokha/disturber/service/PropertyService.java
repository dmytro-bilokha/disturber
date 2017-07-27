package com.dmytrobilokha.disturber.service;

import com.dmytrobilokha.disturber.Constants;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import static com.dmytrobilokha.disturber.Constants.NEW_LINE;

/**
 * Service to manage application properties
 */
@ApplicationScoped
public class PropertyService {

    private static final String DEFAULT_PROPERTIES_FILE = "/defaults.properties";

    private Properties appProperties;
    private Path configFilePath;

    @PostConstruct
    public void init() {
        String configDirLocation = System.getProperty(Constants.CONFIG_DIR_PROPERTY_KEY);
        if (configDirLocation == null)
            throw new IllegalStateException("Configuration directory is not defined");
        String configFileLocation = configDirLocation + Constants.FILE_SEPARATOR + Constants.APPLICATION_NAME + ".properties";
        configFilePath = Paths.get(configFileLocation);
        if (Files.notExists(configFilePath))
            copyDefaultsToConfig();
        loadProperties();
        validateProperties();
    }

    private void copyDefaultsToConfig() {
        try {
            Files.copy(getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILE), configFilePath);
        } catch (IOException | SecurityException ex) {
            throw new IllegalStateException("Unable to copy default properties to the config file '"
                    + configFilePath + '\'', ex);
        }
    }

    private void loadProperties() {
        appProperties = new Properties();
        try(Reader configFileReader = Files.newBufferedReader(configFilePath)) {
            appProperties.load(configFileReader);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load application properties from file '"
                    + configFilePath + '\'', ex);
        }
    }

    public String getAsString(Property property) {
        return appProperties.getProperty(property.key);
    }

    public void setAsString(Property property, String newValue) {
        String errorMessage = checkPropertyValue(property, newValue);
        if (errorMessage != null)
            throw new IllegalArgumentException(errorMessage);
        if (newValue == null) {
            appProperties.remove(property.key);
            return;
        }
        appProperties.setProperty(property.key, newValue);
    }

    public Integer getAsInteger(Property property) {
        String valueAsString = getAsString(property);
        if (valueAsString == null)
            return null;
        return Integer.valueOf(valueAsString);
    }

    public void setAsInteger(Property property, Integer newValue) {
        if (newValue == null)
            setAsString(property, null);
        else
            setAsString(property, newValue.toString());
    }

    public void saveProperties() throws IOException {
        try (Writer configFileWriter = Files.newBufferedWriter(configFilePath
                , StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            appProperties.store(configFileWriter, "Properties update");
        }
    }

    private void validateProperties() {
        StringBuilder errorMessageBuilder = new StringBuilder();
        for (Property property : Property.values()) {
            String errorMessage = checkPropertyValue(property, appProperties.getProperty(property.key));
            if (errorMessage != null)
                errorMessageBuilder.append(errorMessage).append(NEW_LINE);
        }
        if (errorMessageBuilder.length() != 0)
            throw new IllegalStateException("Properties file '" + configFilePath
                    + "' is invalid. Found following error(s):" + NEW_LINE + errorMessageBuilder.toString());
    }

    private String checkPropertyValue(Property property, String value) {
        if (property.isMandatory && value == null) {
            return "Property '" + property + "' is mandatory, but got null instead of value";
        }
        if (property.isConvertableToInt && !isValidIntString(value)) {
            return "Property '" + property + "' should be convertable to integer, but its value '"
                            + appProperties.getProperty(property.key) + "' does not represent valid integer";
        }
        return null;
    }

    private boolean isValidIntString(String intString) {
        if (intString == null)
            return true;
        try {
            Integer.valueOf(intString);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public enum Property {
        COLOR("color", true, false),
        PROPERTIES_VERSION("properties.version", true, true);

        private String key;
        private boolean isMandatory;
        private boolean isConvertableToInt;

        Property(String key, boolean isMandatory, boolean isConvertableToInt) {
            this.key = key;
            this.isMandatory = isMandatory;
            this.isConvertableToInt = isConvertableToInt;
        }

    }
}
