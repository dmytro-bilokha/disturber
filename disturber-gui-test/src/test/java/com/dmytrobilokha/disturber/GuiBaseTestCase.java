package com.dmytrobilokha.disturber;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.testfx.framework.junit.ApplicationTest;

import java.util.ResourceBundle;

/**
 * Class used as parent for GUI tests
 */
public abstract class GuiBaseTestCase extends ApplicationTest {

    private Parent parent;

    static {
        if (System.getProperty("test.headless") != null) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = getFXMLLoader();
        parent = fxmlLoader.load();
        Scene scene = new Scene(parent, 600, 400);
        stage.setTitle("Test " + getClass());
        stage.setScene(scene);
        stage.show();
    }

    protected FXMLLoader getFXMLLoader() {
        FXMLLoader fxmlLoader = getCustomizedFXMLLoader();
        if (fxmlLoader == null)
                fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(getFxmlLocation()));
        fxmlLoader.setResources(getResources());
        Callback<Class<?>, Object> controllerFactory = getControllerFactory();
        if (controllerFactory != null)
            fxmlLoader.setControllerFactory(controllerFactory);
        return fxmlLoader;
    }

    protected Parent getParent() {
        return parent;
    }

    protected abstract String getFxmlLocation();

    protected ResourceBundle getResources() {
        return ResourceBundle.getBundle("messages");
    }

    protected Callback<Class<?>, Object> getControllerFactory() {
        return null;
    }

    protected FXMLLoader getCustomizedFXMLLoader() {
        return null;
    }
}
