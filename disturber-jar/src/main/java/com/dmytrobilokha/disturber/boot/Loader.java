package com.dmytrobilokha.disturber.boot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

public class Loader extends Application {

    public static final String APPLICATION_NAME = "disturber";
    public static final String APPLICATION_TITLE = "Java FX Application Skeleton";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String CONFIG_DIR_PROPERTY_KEY = APPLICATION_NAME + ".configdir";
    private static final String LOGFILE_PROPERTY_KEY = APPLICATION_NAME + ".logfile";
    private static final String MAIN_FXML = "/fxml/TabPanel.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        String configDirLocation = initConfigDir();
        setLogfileLocation(configDirLocation);
        ContainerManager.startContainer();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Starting " + APPLICATION_NAME + "...");
        FXMLLoader fxmlLoader = ContainerManager.getBeanByClass(FXMLLoader.class);
        if (fxmlLoader == null)
            throw new IllegalStateException("Failed to get FXMLLoader from ContainerManager");
        fxmlLoader.setLocation(getClass().getResource(MAIN_FXML));
        Parent panel = fxmlLoader.load();
        Scene scene = new Scene(panel, 600, 400);
        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        ContainerManager.stopContainer();
    }

    private String initConfigDir() {
        String configDirLocation = System.getProperty(CONFIG_DIR_PROPERTY_KEY);
        if (configDirLocation != null)
            return configDirLocation;
        configDirLocation = System.getProperty("user.home") + FILE_SEPARATOR
                + ".config" + FILE_SEPARATOR + APPLICATION_NAME;
        Path configDirPath = Paths.get(configDirLocation);
        if (Files.exists(configDirPath) && Files.isDirectory(configDirPath)) {
            checkConfigDirValid(configDirPath);
        } else {
            createConfigDir(configDirPath);
        }
        System.setProperty(CONFIG_DIR_PROPERTY_KEY, configDirLocation);
        return  configDirLocation;
    }

    private void checkConfigDirValid(Path configDirPath) {
        try {
            if (!Files.isReadable(configDirPath))
                throw new IllegalStateException("Configuration directory " + configDirPath + " is not readable");
            if (!Files.isWritable(configDirPath))
                throw new IllegalStateException("Configuration directory " + configDirPath + " is not writable");
            if (!Files.isExecutable(configDirPath))
                throw new IllegalStateException("Configuration directory " + configDirPath
                        + " is not accessible to traverse");
        } catch (SecurityException ex) {
            throw new IllegalStateException("Unable to check validity of the configuration directory "
                    + configDirPath + " because of system security restrictions", ex);
        }
    }

    private void createConfigDir(Path configDirPath) {
        try {
            boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
            if (isPosix)
                Files.createDirectories(configDirPath, buildConfigDirPermissions());
            else
                Files.createDirectories(configDirPath);
        } catch (IOException | UnsupportedOperationException | SecurityException ex) {
            throw new IllegalStateException("Unable to create config directory " + configDirPath, ex);
        }
    }

    private FileAttribute buildConfigDirPermissions() {
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        return PosixFilePermissions.asFileAttribute(permissions);
    }

    private void setLogfileLocation(String configDirLocation) {
        if (System.getProperty(LOGFILE_PROPERTY_KEY) != null)
            return;
        System.setProperty(LOGFILE_PROPERTY_KEY, configDirLocation + FILE_SEPARATOR + APPLICATION_NAME + ".log");
    }

}
