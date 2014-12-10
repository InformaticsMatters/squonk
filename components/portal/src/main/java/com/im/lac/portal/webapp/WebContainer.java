package com.im.lac.portal.webapp;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class WebContainer {

    private static final Logger logger = Logger.getLogger(WebContainer.class.getName());
    private static final Properties properties;

    private static final String WEBCONTAINER_PORT = "webcontainer_port";
    private static final String WEBCONTAINER_WEBAPP = "webcontainer_webapp";

    private static final String DEFAULT_WEBAPP = "webapp";
    private static final String DEFAULT_PORT = "8080";

    static {
        properties = new Properties();
        try {
            File propFile = new File("webservices.properties");
            if (propFile.exists()) {
                properties.load(new FileReader(propFile));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(WebContainer.class.getName());
        try {
            WebContainer server = new WebContainer();
            server.start();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, null, t);
            System.exit(1);
        }
    }

    private void startWebServer() throws Exception {
        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        int port = new Integer(properties.getProperty(WEBCONTAINER_PORT, DEFAULT_PORT));
        connector.setPort(port);
        server.addConnector(connector);
        String webapp = properties.getProperty(WEBCONTAINER_WEBAPP, DEFAULT_WEBAPP);
        WebAppContext wac = new WebAppContext(webapp, "/");
        server.setHandler(wac);
        server.setStopAtShutdown(true);
        server.start();
    }

    public void start() throws Exception {
        startWebServer();
    }

}
