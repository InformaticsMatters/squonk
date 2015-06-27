package com.im.lac.job.service.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.SimpleRegistry;
import java.util.logging.Logger;


/**
 *
 * @author timbo
 */
public class CamelLifeCycle {

    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());

    public void beforeStart(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void afterStart(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void beforeStop(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void afterStop(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void beforeAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void afterAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

}
