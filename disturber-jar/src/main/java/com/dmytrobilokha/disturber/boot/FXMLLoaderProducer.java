package com.dmytrobilokha.disturber.boot;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * The producer to produce FXMLLoader with customized controller factory.
 */
public class FXMLLoaderProducer {

    private static final Logger LOG = LoggerFactory.getLogger(FXMLLoaderProducer.class);

    private final BeanManager beanManager;

    @Inject
    public FXMLLoaderProducer(BeanManager beanManager) {
        LOG.info("FXMLLoaderProducer constructor called with beanManager={}", beanManager);
        this.beanManager = beanManager;
    }

    @Produces
    @Dependent
    public FXMLLoader produce(InjectionPoint injectionPoint) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        LOG.debug("Producer are going to produce FXMLLoader={}", fxmlLoader);
        fxmlLoader.setControllerFactory(new ControllerFactory());
        fxmlLoader.setResources(ResourceBundle.getBundle("messages"));
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
