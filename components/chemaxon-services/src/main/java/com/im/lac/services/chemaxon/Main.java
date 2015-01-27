package com.im.lac.services.chemaxon;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.postgresql.ds.PGSimpleDataSource;

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
        DataSource ds = createDataSource();
        SimpleRegistry reg = new SimpleRegistry();
        final CamelContext camelContext = new DefaultCamelContext(reg);
        camelContext.addRoutes(new CalculatorsRouteBuilder());
        camelContext.addRoutes(new DatabaseRouteBuilder(ds));
        camelContext.addRoutes(new RestRouteBuilder(reg));
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

}
