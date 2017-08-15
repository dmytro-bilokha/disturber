package com.dmytrobilokha.disturber.config.property;

import com.dmytrobilokha.disturber.Constants;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import static com.dmytrobilokha.disturber.Constants.NEW_LINE;

/**
 * Service to manage application properties.
 * Not thread-safe.
 */
@ApplicationScoped
public class PropertyService {

    private static final String DEFAULT_PROPERTIES_FILE = "/defaults.properties";

    private Map<Property, Object> propertyMap;
    private Path configFilePath;

    @PostConstruct
    void init() {
        String configDirLocation = System.getProperty(Constants.CONFIG_DIR_PROPERTY_KEY);
        if (configDirLocation == null)
            throw new IllegalStateException("Configuration directory is not defined");
        String configFileLocation = configDirLocation + Constants.FILE_SEPARATOR + Constants.PROPERTIES_FILE_NAME;
        configFilePath = Paths.get(configFileLocation);
        if (Files.notExists(configFilePath))
            copyDefaultsToConfig();
        Properties appProperties = loadPropertiesFile();
        propertyMap = parseProperties(appProperties);
    }

    private void copyDefaultsToConfig() {
        try (InputStream defaultPropertiesInputStream = getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
             BufferedInputStream defaultPropertiesBufferedInputStream
                     = new BufferedInputStream(defaultPropertiesInputStream)){
            Files.copy(defaultPropertiesBufferedInputStream, configFilePath);
        } catch (IOException | SecurityException ex) {
            throw new IllegalStateException("Unable to copy default properties to the config file '"
                    + configFilePath + '\'', ex);
        }
    }

    private Properties loadPropertiesFile() {
        Properties appProperties = new Properties();
        try (Reader configFileReader = Files.newBufferedReader(configFilePath)) {
            appProperties.load(configFileReader);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load application properties from file '"
                    + configFilePath + '\'', ex);
        }
        return appProperties;
    }

    private Map<Property, Object> parseProperties(Properties appProperties) {
        Map<Property, Object> parseResultMap = new EnumMap<>(Property.class);
        StringBuilder errorMessageBuilder = new StringBuilder();
        for (Property property : Property.values()) {
            String value = appProperties.getProperty(property.key);
            Object parsedValue = property.parseValue(value, errorMessageBuilder);
            if (parsedValue != null) {
                parseResultMap.put(property, parsedValue);
            }
        }
        if (errorMessageBuilder.length() > 0)
            throw new IllegalStateException("Application properties are invalid. Found following errors: "
                        + NEW_LINE + errorMessageBuilder.toString());
        return parseResultMap;
    }

    public String getString(Property property) {
        return getAndCastValue(property, String.class);
    }

    public void setString(Property property, String value) {
        checkAndSetValue(property, value, String.class);
    }

    public Integer getInteger(Property property) {
        return getAndCastValue(property, Integer.class);
    }

    public void setInteger(Property property, Integer value) {
        checkAndSetValue(property, value, Integer.class);
    }

    public <T extends Enum<T>> T getEnum(Property property) {
        return (T) getAndCastValue(property, Enum.class);
    }

    public <T extends Enum<T>> void setEnum(Property property, T value) {
        checkAndSetValue(property, value, value == null ? property.clazz : value.getClass());
    }

    public Boolean getBoolean(Property property) {
        return getAndCastValue(property, Boolean.class);
    }

    public void setBoolean(Property property, Boolean value) {
        checkAndSetValue(property, value, Boolean.class);
    }

    private <T> T getAndCastValue(Property property, Class<T> clazz) {
        if (clazz.isAssignableFrom(property.clazz))
            return clazz.cast(propertyMap.get(property));
        throw new IllegalArgumentException("Returning value of the property '" + property + "' as '"
                + clazz.getName() + " is not supported, because property value type is '" + property.clazz.getName() + '\'');
    }

    private void checkAndSetValue(Property property, Object value, Class clazz) {
        if (value == null) {
            if (!property.clazz.isAssignableFrom(clazz))
                throwWrongSetArgument(property, clazz);
            if (property.isMandatory)
                throw new IllegalArgumentException("Property '" + property + "' is mandatory, setting null is not allowed");
            propertyMap.remove(property);
            return;
        }
        if (!property.clazz.isAssignableFrom(value.getClass()))
            throwWrongSetArgument(property, value.getClass());
        propertyMap.put(property, value);
    }

    private void throwWrongSetArgument(Property property, Class clazz) {
        throw new IllegalArgumentException("Setting value of the property '" + property + "' as '"
                + clazz.getName() + " is not supported, because property value type is '" + property.clazz.getName() + '\'');
    }

    public void saveProperties() throws IOException {
        try (Writer configFileWriter = Files.newBufferedWriter(configFilePath
                , StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            Properties appProperties = propertyMapToProperties();
            appProperties.store(configFileWriter, "Properties updated at " + LocalDateTime.now());
        }
    }

    private Properties propertyMapToProperties() {
        Properties appProperties = new Properties();
        for (Map.Entry<Property, Object> propertyObjectEntry : propertyMap.entrySet()) {
            Property property = propertyObjectEntry.getKey();
            appProperties.put(property.key, property.valueToString(propertyObjectEntry.getValue()));
        }
        return appProperties;
    }

}
