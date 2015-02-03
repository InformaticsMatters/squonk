package com.im.lac.services.chemaxon;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Launcher for the Camel context
 *
 * @author timbo
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {

        DataSource ds = createDataSource();
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("contextHandler", getContextHandler());
        final CamelContext camelContext = new DefaultCamelContext(registry);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("jetty://http://0.0.0.0:8080/jetty/ping?handlers=#contextHandler")
                        .log("Testing Jetty")
                        .transform().constant("Jetty Running\n");;
            }
        });
        camelContext.addRoutes(new CalculatorsRouteBuilder());
        camelContext.addRoutes(new DescriptorsRouteBuilder());
        camelContext.addRoutes(new DatabaseRouteBuilder(ds));
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

    private static DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        String server = System.getenv("CHEMCENTRAL_DB_SERVER");
        ds.setServerName(server != null ? server : "localhost");
        LOG.log(Level.INFO, "DB sever: {0}", ds.getServerName());
        String portEnv = System.getenv("CHEMCENTRAL_DB_PORT");
        ds.setPortNumber(portEnv != null ? new Integer(portEnv) : 5432);
        LOG.log(Level.INFO, "DB port: {0}", ds.getPortNumber());
        ds.setDatabaseName("chemcentral");
        String username = System.getenv("CHEMCENTRAL_DB_USERNAME");
        ds.setUser(username != null ? username : "chemcentral");
        String password = System.getenv("CHEMCENTRAL_DB_PASSWORD");
        ds.setPassword(password != null ? password : "chemcentral");
        return ds;
    }

    private static ResourceHandler getContextHandler() throws IOException {
        ResourceHandler rh = new ResourceHandler();
        String root = System.getenv("HTML_DOC_ROOT");
        if (root == null) {
            root = "src/main/html/";
        }
        LOG.log(Level.INFO, "Document path is {0}", root);
        rh.setBaseResource(Resource.newResource(root));
        return rh;
    }

}
