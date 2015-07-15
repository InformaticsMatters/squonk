package com.im.lac.services.chemaxon;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

/**
 * Launcher for the Camel context
 *
 * @author timbo
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {

        SimpleRegistry registry = new SimpleRegistry();
        final CamelContext camelContext = new DefaultCamelContext(registry);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                restConfiguration().component("jetty").host("0.0.0.0").port(8080);

                from("jetty://http://0.0.0.0:8080/jetty/ping")
                        .log("Testing Jetty")
                        .transform().constant("Jetty Running\n");
            }
        });
        //camelContext.addRoutes(new MoleculeUtilsRouteBuilder());
        camelContext.addRoutes(new CalculatorsRouteBuilder());
        //camelContext.addRoutes(new DescriptorsRouteBuilder());
        //camelContext.addRoutes(new ReactorRouteBuilder());
        camelContext.addRoutes(new RestRouteBuilder());

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

        LOG.log(Level.INFO, "Starting CamelContext");
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    camelContext.start();
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Failed to start Camel", ex);
                }
            }
        };
        t.start();
        t.join();
    }
}