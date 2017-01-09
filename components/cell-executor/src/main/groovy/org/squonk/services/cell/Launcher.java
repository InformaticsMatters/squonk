package org.squonk.services.cell;

import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.se.events.ContainerShutdown;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by timbo on 08/01/16.
 */
@Singleton
public class Launcher {

    private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

//    public static void main(String[] args) throws Exception {
//        final Weld weld = new Weld();
//        final WeldContainer container = weld.initialize();
//
//        LOG.info("Launching ...");
//        final CamelRoutes routes = container.select(CamelRoutes.class).getServiceDescriptors();
//        routes.init();
//        routes.start();
//
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                LOG.info("Shutting down Camel ...");
//                try {
//                    routes.stop();
//                } catch (Exception ex) {
//                    LOG.log(Level.WARNING, "Failed to shutdown Camel context cleanly", ex);
//                }
//                LOG.info("Shutting down Weld container ...");
//                container.shutdown();
//                LOG.info("Shutdown complete");
//            }
//        });
//
//        Thread.currentThread().join();
//    }

    public void start(@Observes ContainerInitialized event, @Parameters List<String> parameters) {
        LOG.info("Starting ...");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void start(@Observes ContainerShutdown event) {
        LOG.info("Stopping ...");
    }
}
