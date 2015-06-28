package com.im.lac.services.camel;

import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.dataset.service.DatasetServiceImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.SimpleRegistry;
import com.im.lac.services.util.Utils;
import com.im.lac.services.job.service.JobHandler;
import com.im.lac.services.job.service.SimpleJobStore;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 *
 * @author timbo
 */
public class CamelLifeCycle {

    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());

    private final DataSource dataSource;

    public CamelLifeCycle() {
        this.dataSource = Utils.createDataSource();

    }

    public CamelLifeCycle(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void beforeStart(CamelContext context, SimpleRegistry r) throws Exception {
            LOG.info("Creating DatasetService");
            DatasetServiceImpl datasetService = new DatasetServiceImpl(dataSource);
            datasetService.createTables();
            r.put(Constants.DATASET_HANDLER, new DatasetHandler(datasetService, "/tmp/datasetcache"));
            r.put(Constants.JOB_HANDLER, new JobHandler());
            r.put(Constants.JOB_STORE, new SimpleJobStore());
    }

    public void afterStart(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void beforeStop(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void afterStop(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void beforeAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

    public void afterAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        // noop
    }

}
