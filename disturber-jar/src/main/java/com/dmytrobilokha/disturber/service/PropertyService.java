package com.dmytrobilokha.disturber.service;

import com.dmytrobilokha.disturber.boot.Loader;

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
        String configDirLocation = System.getProperty(Loader.CONFIG_DIR_PROPERTY_KEY);
        if (configDirLocation == null)
            throw new IllegalStateException("Configuration directory is not defined");
        String configFileLocation = configDirLocation + Loader.FILE_SEPARATOR + Loader.APPLICATION_NAME + ".properties";
        configFilePath = Paths.get(configFileLocation);
        if (Files.notExists(configFilePath))
            copyDefaultsToConfig(configFilePath);
        loadProperties(configFilePath);
    }

    private void copyDefaultsToConfig(Path configFilePath) {
        try {
            Files.copy(getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILE), configFilePath);
        } catch (IOException | SecurityException ex) {
            throw new IllegalStateException("Unable to copy default properties to the config file '"
                    + configFilePath + '\'', ex);
        }
    }

    private void loadProperties(Path configFilePath) {
        appProperties = new Properties();
        try(Reader configFileReader = Files.newBufferedReader(configFilePath)) {
            appProperties.load(configFileReader);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load application properties from file '"
                    + configFilePath + '\'', ex);
        }
    }

    public String getAsString(String key) {
        return appProperties.getProperty(key);
    }

    public Integer getAsInteger(String key) {
        String valueAsString = getAsString(key);
        if (valueAsString == null)
            return null;
        return Integer.valueOf(valueAsString);
    }

    public void set(String key, Object value) {
        if (value == null) {
            appProperties.remove(key);
            return;
        }
        appProperties.setProperty(key, value.toString());
    }

    public void saveProperties() throws IOException {
        try (Writer configFileWriter = Files.newBufferedWriter(configFilePath
                , StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            appProperties.store(configFileWriter, "Properties update");
        }
    }

}
