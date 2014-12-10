package com.im.lac.portal.webapp;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.cdi.CDIComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.jboss.weld.environment.servlet.Listener;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletConfig;

/**
 * @author simetrias
 */
public class CDIJerseyServlet extends ServletContainer {

    @Override
    protected void configure(ServletConfig sc, ResourceConfig rc, WebApplication wa) {
        super.configure(sc, rc, wa);
        BeanManager beanManager = (BeanManager) sc.getServletContext().getAttribute(Listener.BEAN_MANAGER_ATTRIBUTE_NAME);
        rc.getSingletons().add(new CDIComponentProviderFactory(beanManager, rc, wa));
    }
}
