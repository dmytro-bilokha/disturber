package com.dmytrobilokha.disturber.boot;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * The class to manage CDI container
 */
class ContainerManager {

    private static final Logger LOG = LoggerFactory.getLogger(ContainerManager.class);
    private static ContainerLifecycle lifecycle = null;

    private ContainerManager() {
        //Not going to instantiate utility class
    }

    static void startContainer() {
        if (lifecycle != null)
            throw new IllegalStateException("Unable to start the CDI container, seems like it is already started");
        LOG.info("Starting JUL->SLF4J logging bridge for the OpenWebBeans");
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LOG.info("Starting OpenWebBeans CDI container");
        lifecycle = WebBeansContext.getInstance().getService(ContainerLifecycle.class);
        lifecycle.startApplication(null);
    }

    static void stopContainer() {
        if (lifecycle == null)
            throw new IllegalStateException("Unable to stop the CDI container, seems like it wasn't started");
        lifecycle.stopApplication(null);
    }

    static <T> T getBeanByClass(Class<T> beanClass) {
        if (lifecycle == null)
            throw new IllegalStateException("Unable to get bean from class "
                    + beanClass + " the CDI container hasn't been started");
        BeanManager beanManager = lifecycle.getBeanManager();
        Bean<?> bean = beanManager.resolve(beanManager.getBeans(beanClass));
        return  (T) beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean));
    }

}
