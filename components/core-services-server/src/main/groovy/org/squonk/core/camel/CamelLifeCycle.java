package org.squonk.core.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.ThreadPoolProfile;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.core.ServerConstants;
import org.squonk.core.service.user.UserHandler;
import org.squonk.core.service.user.UserPostgresClient;
import org.squonk.core.util.Utils;

import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class CamelLifeCycle {

    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());
    

    private final DataSource dataSource;

    public CamelLifeCycle() {
        this(Utils.createDataSource());
    }

    public CamelLifeCycle(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void beforeStart(CamelContext context, SimpleRegistry r) throws Exception {
        //context.getShutdownStrategy().setTimeout(10);
        LOG.fine("beforeStart()");
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(4).maxPoolSize(50).build();
        context.getExecutorServiceManager().registerThreadPoolProfile(profile);
    }

    public void afterStart(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("afterStart()");
        // noop
    }

    public void beforeStop(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("beforeStop()");
        // noop
    }

    public void afterStop(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("afterStop()");

    }

    public void beforeAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("beforeAddRoutes()");
        r.put(ServerConstants.USER_HANDLER, new UserHandler(new UserPostgresClient(dataSource)));
    }

    public void afterAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("afterAddRoutes()");
        // noop
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
