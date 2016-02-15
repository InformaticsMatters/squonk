package org.squonk.core.camel;

import org.squonk.core.ServerConstants;
import org.squonk.core.dataset.service.DatasetHandler;
import org.squonk.core.job.service.MemoryJobStatusClient;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.core.dataset.service.DatasetService;
import org.squonk.core.discovery.service.ServiceDescriptorStore;
import org.squonk.core.user.UserService;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.squonk.core.util.Utils;
import org.squonk.core.user.UserHandler;
import java.util.logging.Logger;

import javax.sql.DataSource;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.spi.ThreadPoolProfile;
import org.squonk.core.discovery.service.ServiceDiscoveryRouteBuilder;

/**
 *
 * @author timbo
 */
public class CamelLifeCycle {

    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());
    

    private final DataSource dataSource;
    private DatasetService datasetService;
    //private String datasetsTableName = DatasetService.DEFAULT_TABLE_NAME;
    //private boolean createTables = "true".equals(System.getenv("CHEMCENTRAL_AUTO_CREATE"));
    //private boolean dropTables = "true".equals(System.getenv("CHEMCENTRAL_AUTO_CREATE"));

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
//        if (dropTables && datasetService != null) {
//            //datasetService.dropTables();
//            LOG.log(Level.INFO, "Tables dropped: {0}", datasetsTableName);
//        }
    }

    public void beforeAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("beforeAddRoutes()");
//        LOG.log(Level.INFO, "CHEMCENTRAL_AUTO_CREATE: {0}", System.getenv("CHEMCENTRAL_AUTO_CREATE"));
//        LOG.log(Level.INFO, "Creating DatasetService: {0} {1} {2}", new Object[]{datasetsTableName, createTables, dropTables});
        
        //context.setStreamCaching(true);
        
        datasetService = new DatasetService(dataSource);
//        datasetService = new DatasetService(dataSource, datasetsTableName, createTables, dropTables);
//        if (dropTables) {
//            datasetService.dropTables();
//            LOG.log(Level.INFO, "Tables dropped: {0}", datasetsTableName);
//        }
//        if (createTables) {
//            datasetService.createTables();
//            LOG.log(Level.INFO, "Tables created: {0}", datasetsTableName);
//        }
        
        ServiceDescriptorStore serviceDescriptorStore = new ServiceDescriptorStore();
        serviceDescriptorStore.addServiceDescriptors("ignored", ServiceDiscoveryRouteBuilder.TEST_SERVICE_DESCRIPTORS);
        r.put(ServerConstants.SERVICE_DESCRIPTOR_STORE, serviceDescriptorStore);
        r.put(ServerConstants.DATASET_HANDLER, new DatasetHandler(datasetService, "/tmp/datasetcache"));
//        r.put(ServerConstants.JOB_HANDLER, new JobHandler());
        r.put(ServerConstants.USER_HANDLER, new UserHandler(new UserService(dataSource)));

    }

    public void afterAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("afterAddRoutes()");
        // noop
    }

//    public boolean isCreateTables() {
//        return createTables;
//    }
//
//    public void setCreateTables(boolean createTables) {
//        this.createTables = createTables;
//    }
//
//    public boolean isDropTables() {
//        return dropTables;
//    }
//
//    public void setDropTables(boolean dropTables) {
//        this.dropTables = dropTables;
//    }
//
//    public String getDatasetsTableName() {
//        return datasetsTableName;
//    }
//
//    public void setDatasetsTableName(String datasetsTableName) {
//        this.datasetsTableName = datasetsTableName;
//    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
