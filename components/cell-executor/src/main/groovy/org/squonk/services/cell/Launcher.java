package org.squonk.services.cell;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 08/01/16.
 */
public class Launcher {

    private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

    public static void main(String[] args) throws Exception {
        final Weld weld = new Weld();
        final WeldContainer container = weld.initialize();

        LOG.info("Launching ...");
        final CamelRoutes routes = container.select(CamelRoutes.class).get();
        routes.init();
        routes.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOG.info("Shutting down Camel ...");
                try {
                    routes.stop();
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Failed to shutdown Camel context cleanly", ex);
                }
                LOG.info("Shutting down Weld container ...");
                container.shutdown();
                LOG.info("Shutdown complete");
            }
        });

        Thread.currentThread().join();
    }
}
