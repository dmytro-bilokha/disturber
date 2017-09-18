package com.dmytrobilokha.disturber.viewcontroller;

import com.dmytrobilokha.disturber.boot.FXMLLoaderProducer;
import com.dmytrobilokha.disturber.viewcontroller.main.MainLayoutController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

/**
 * The factory to create views on-demand
 */
@ApplicationScoped
public class ViewFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ViewFactory.class);
    private static final String MAIN_LAYOUT_FXML = "main/MainLayout.fxml";

    private FXMLLoaderProducer fxmlLoaderProducer;

    protected ViewFactory() {
        //No args constructor to keep CDI framework happy
    }

    @Inject
    public ViewFactory(FXMLLoaderProducer fxmlLoaderProducer) {
        this.fxmlLoaderProducer = fxmlLoaderProducer;
    }

    public Parent produceMainLayout() {
        ViewControllerHolder<Parent, MainLayoutController> viewControllerHolder = load(MAIN_LAYOUT_FXML);
        return viewControllerHolder.view;
    }

    private <V, C> ViewControllerHolder<V, C> load(String fxmlLocation) {
        FXMLLoader fxmlLoader = fxmlLoaderProducer.produce();
        fxmlLoader.setLocation(getClass().getResource(fxmlLocation));
        try {
            V view = fxmlLoader.load();
            C controller =  fxmlLoader.getController();
            return new ViewControllerHolder<>(view, controller);
        } catch (IOException ex) {
            LOG.error("Failed to load view {} because of internal error", fxmlLocation, ex);
            throw new IllegalStateException("Failed to load view" + fxmlLocation + " because of internal error");
        }
    }

    private static class ViewControllerHolder<V, C> {
        private final V view;
        private final C controller;

        private ViewControllerHolder(V view, C controller) {
            this.view = view;
            this.controller = controller;
        }
    }

}
