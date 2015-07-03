package com.im.lac.services.camel;

import com.im.lac.services.ServerConstants;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.dataset.service.DatasetServiceImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.SimpleRegistry;
import com.im.lac.services.util.Utils;
import com.im.lac.services.job.service.JobHandler;
import com.im.lac.services.job.service.SimpleJobStore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 *
 * @author timbo
 */
public class CamelLifeCycle {

    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());

    private final DataSource dataSource;
    private DatasetServiceImpl datasetService;
    private String datasetsTableName = DatasetServiceImpl.DEFAULT_TABLE_NAME;
    private boolean createTables = false;
    private boolean dropTables = false;

    public CamelLifeCycle() {
        this(Utils.createDataSource());
    }

    public CamelLifeCycle(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void beforeStart(CamelContext context, SimpleRegistry r) throws Exception {
        //context.getShutdownStrategy().setTimeout(10);
        LOG.fine("beforeStart()");
        // noop
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
        if (dropTables && datasetService != null) {
            //datasetService.dropTables();
            LOG.log(Level.INFO, "Tables dropped: {0}", datasetsTableName);
        }
    }

    public void beforeAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("beforeAddRoutes()");
        LOG.fine("Creating DatasetService");
        datasetService = new DatasetServiceImpl(dataSource, datasetsTableName, createTables, dropTables);
        if (dropTables) {
            datasetService.dropTables();
            LOG.log(Level.INFO, "Tables dropped: {0}", datasetsTableName);
        }
        if (createTables) {
            datasetService.createTables();
            LOG.log(Level.INFO, "Tables created: {0}", datasetsTableName);
        }
        r.put(ServerConstants.DATASET_HANDLER, new DatasetHandler(datasetService, "/tmp/datasetcache"));
        r.put(ServerConstants.JOB_HANDLER, new JobHandler());
        r.put(ServerConstants.JOB_STORE, new SimpleJobStore());
    }

    public void afterAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("afterAddRoutes()");
        // noop
    }

    public boolean isCreateTables() {
        return createTables;
    }

    public void setCreateTables(boolean createTables) {
        this.createTables = createTables;
    }

    public boolean isDropTables() {
        return dropTables;
    }

    public void setDropTables(boolean dropTables) {
        this.dropTables = dropTables;
    }

    public String getDatasetsTableName() {
        return datasetsTableName;
    }

    public void setDatasetsTableName(String datasetsTableName) {
        this.datasetsTableName = datasetsTableName;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
