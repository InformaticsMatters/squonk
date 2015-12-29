package org.squonk.chemaxon.services;

import com.im.lac.camel.CamelCommonConstants;
import java.util.logging.Logger;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.component.servletlistener.CamelContextLifecycle;
import org.apache.camel.component.servletlistener.ServletCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.ThreadPoolProfile;

/**
 *
 * @author timbo
 */
public class CamelLifeCycle implements CamelContextLifecycle<SimpleRegistry> {

    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());

    @Override
    public void beforeStart(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.beforeStart()");
    }

    @Override
    public void afterStart(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.afterStart()");
    }

    @Override
    public void beforeStop(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.beforeStop()");
    }

    @Override
    public void afterStop(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.afterStop()");
    }

    @Override
    public void beforeAddRoutes(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.beforeAddRoutes()");
        LOG.fine("Creating custom thread pool profile named " + CamelCommonConstants.CUSTOM_THREAD_POOL_NAME);
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(4).maxPoolSize(50).build();
        scc.getExecutorServiceManager().registerThreadPoolProfile(profile);
    }

    @Override
    public void afterAddRoutes(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.afterAddRoutes()");
    }

}
