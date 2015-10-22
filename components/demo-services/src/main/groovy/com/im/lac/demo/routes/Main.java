package com.im.lac.demo.routes;

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
import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;

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
        
        // setup ActiveMQ
        //camelContext.addComponent("activemq", activeMQComponent("vm://localhost?broker.persistent=false"));
        //camelContext.addComponent("activemq", activeMQComponent("tcp://localhost:61616?broker.persistent=false"));
        
        // add the routes
                
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                
                restConfiguration().component("jetty").host("0.0.0.0").port(8080);

                from("jetty://http://0.0.0.0:8080/jetty/ping?handlers=#contextHandler")
                        .log("Testing Jetty")
                        .transform().constant("Jetty Running\n");
            }
        });
        if (System.getenv("RDBASE") != null) {
            LOG.info("Adding RDKit based routes");
            camelContext.addRoutes(new RDKitRouteBuilder());
        } else {
            LOG.info("Skipping RDKit based routes");
        }
        camelContext.addRoutes(new MoleculeUtilsRouteBuilder());
        camelContext.addRoutes(new RDKitServerRouteBuilder());
        camelContext.addRoutes(new CalculatorsRouteBuilder());
        camelContext.addRoutes(new DescriptorsRouteBuilder());
        camelContext.addRoutes(new DatabaseRouteBuilder(ds));
        camelContext.addRoutes(new ReactorRouteBuilder());
        camelContext.addRoutes(new RestRouteBuilder());
        camelContext.addRoutes(new FileServicesRouteBuilder(ds));
        //camelContext.addRoutes(new JmsRouteBuilder());

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

    public static DataSource createDataSource() {
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
