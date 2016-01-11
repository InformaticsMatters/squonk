package org.squonk.services.cell;

import org.apache.camel.impl.DefaultCamelContext;

import javax.inject.Inject;

import java.util.logging.Logger;

/**
 * Created by timbo on 07/01/16.
 */
public class CamelRoutes {


    private DefaultCamelContext context;

    @Inject
    private CellExecutorRouteBuilder cellExecutorRouteBuilder;

    private static final Logger LOG = Logger.getLogger(CamelRoutes.class.getName());


    public void init() throws Exception {
        LOG.info("Starting cell executor routes");
        context = new DefaultCamelContext();
        context.addRoutes(cellExecutorRouteBuilder);
    }

    public void start() throws Exception {
        LOG.info("Starting Camel context");
        context.start();

    }

    public void stop() throws Exception {
        LOG.info("Stopping Camel context");
        context.stop();
    }

}
