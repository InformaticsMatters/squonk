package com.im.lac.camel.chemaxon.routes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Launcher for the Camel context
 *
 * @author timbo
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
//        Integer port = ExampleRouteBuilder.DEFAULT_PORT;
//        if (args.length > 0) {
//            port = new Integer(args[0]);
//        }
        final CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new CalculatorsRouteBuilder());
        camelContext.addRoutes(new RestRouteBuilder());
        LOG.log(Level.INFO, "Starting CamelContext");
        camelContext.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Stopping CamelContext");
                try {
                    camelContext.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        while (true) {
            Thread.sleep(1000);
        }
    }

}
