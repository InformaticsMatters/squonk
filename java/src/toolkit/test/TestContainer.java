package toolkit.test;

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
public class TestContainer {

    private static final String TESTCONTAINER_PORT = "testcontainer_port";
    private static final String DEFAULT_PORT = "8081";
    private static final Logger logger = createLogger();
    private static final Properties properties;
    private static Server server;

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

    public TestContainer() {
    }

    private static void startWebServer() throws Exception {
        server = new Server();
        Connector connector = new SelectChannelConnector();
        int portNumber = Integer.valueOf(properties.getProperty(TESTCONTAINER_PORT, DEFAULT_PORT));
        connector.setPort(portNumber);
        server.addConnector(connector);
        WebAppContext wac = new WebAppContext("webapp", "/");
        wac.addServlet(TestRunnerServlet.class, "/test/*");
        server.setHandler(wac);
        server.setStopAtShutdown(true);
        server.start();
    }

    private static Logger createLogger() {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        return Logger.getLogger(TestContainer.class.getName());
    }

    public static void main(String[] args) {
        try {
            TestContainer testContainer;
            testContainer = new TestContainer();
            testContainer.start();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, null, t);
            System.exit(1);
        }
    }

    public void start() throws Exception {
        startWebServer();
        launchTests();
    }

    public void start(Class testClass) throws Exception {
        start(testClass, true);
    }

    public void start(Class testClass, boolean report) throws Exception {
        startWebServer();
        launchTest(testClass, report);
    }

    private void launchTests() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    try {
                        int portNumber = Integer.valueOf(properties.getProperty(TESTCONTAINER_PORT, DEFAULT_PORT));
                        TestRunnerClient client = new TestRunnerClient(portNumber);
                        client.execute();
                    } finally {
                        server.stop();
                        System.exit(0);
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        };
        thread.start();
    }

    private void launchTest(final Class testClass, final boolean report) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    try {
                        int portNumber = Integer.valueOf(properties.getProperty(TESTCONTAINER_PORT, DEFAULT_PORT));
                        TestRunnerClient client = new TestRunnerClient(portNumber);
                        client.execute(testClass);
                    } finally {
                        server.stop();
                        System.exit(0);
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        };
        thread.start();
    }
}
