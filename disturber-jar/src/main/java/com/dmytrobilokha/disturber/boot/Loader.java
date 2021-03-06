package com.dmytrobilokha.disturber.boot;

import com.dmytrobilokha.disturber.Constants;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import com.dmytrobilokha.disturber.viewcontroller.main.MainLayoutController;
import javafx.application.Application;
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

/**
 * The application entry point class. It does some initialization and on start checks.
 */
public class Loader extends Application {

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
        System.out.println("Starting " + Constants.APPLICATION_NAME + "...");
        ViewFactory viewFactory = ContainerManager.getBeanByClass(ViewFactory.class);
        ViewFactory.ViewControllerHolder<Parent, MainLayoutController> mainLayoutHolder = viewFactory.produceMainLayout();
        Scene scene = new Scene(mainLayoutHolder.getView(), 600, 400);
        primaryStage.setTitle(Constants.APPLICATION_TITLE);
        primaryStage.setScene(scene);
        mainLayoutHolder.getController().setMainStage(primaryStage);
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.out.println("Exiting " + Constants.APPLICATION_NAME + "...");
        ContainerManager.stopContainer();
    }

    private String initConfigDir() {
        String configDirLocation = System.getProperty(Constants.CONFIG_DIR_PROPERTY_KEY);
        if (configDirLocation != null)
            return configDirLocation;
        configDirLocation = System.getProperty("user.home") + Constants.FILE_SEPARATOR
                + ".config" + Constants.FILE_SEPARATOR + Constants.APPLICATION_NAME;
        Path configDirPath = Paths.get(configDirLocation);
        if (Files.exists(configDirPath) && Files.isDirectory(configDirPath)) {
            checkConfigDirValid(configDirPath);
        } else {
            createConfigDir(configDirPath);
        }
        System.setProperty(Constants.CONFIG_DIR_PROPERTY_KEY, configDirLocation);
        return  configDirLocation;
    }

    private void checkConfigDirValid(Path configDirPath) {
        String errorMessagePrefix = "Configuration directory '" + configDirPath + "' ";
        try {
            if (!Files.isReadable(configDirPath))
                throw new IllegalStateException(errorMessagePrefix + "is not readable");
            if (!Files.isWritable(configDirPath))
                throw new IllegalStateException(errorMessagePrefix + "is not writable");
            if (!Files.isExecutable(configDirPath))
                throw new IllegalStateException(errorMessagePrefix + "is not accessible to traverse");
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
        if (System.getProperty(Constants.LOGFILE_PROPERTY_KEY) != null)
            return;
        System.setProperty(Constants.LOGFILE_PROPERTY_KEY
                , configDirLocation + Constants.FILE_SEPARATOR + Constants.APPLICATION_NAME + ".log");
    }

}
