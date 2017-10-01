package com.dmytrobilokha.disturber.boot;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * The producer to produce FXMLLoader with customized controller factory.
 */
@ApplicationScoped
public class FXMLLoaderProducer {

    private BeanManager beanManager;

    protected FXMLLoaderProducer() {
        //No args constructor to keep CDI framework happy
    }

    @Inject
    public FXMLLoaderProducer(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Produces
    @Dependent
    public ResourceBundle getMessageBundle() {
        return ResourceBundle.getBundle("messages");
    }

    @Produces
    @Dependent
    public FXMLLoader produce() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(new ControllerFactory());
        fxmlLoader.setResources(getMessageBundle());
        return fxmlLoader;
    }

    private class ControllerFactory implements Callback<Class<?>, Object> {

        @Override
        public Object call(Class controllerClass) {
            Bean<?> bean = beanManager.resolve(beanManager.getBeans(controllerClass));
            Object controllerObject = beanManager.getReference(bean
                    , bean.getBeanClass(), beanManager.createCreationalContext(bean));
            if (controllerObject == null)
                throw new IllegalArgumentException("Failed to get instance of controller for class " + controllerClass);
            return controllerObject;
        }

    }

}
