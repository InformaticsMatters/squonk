package com.im.lac.dataset.service.camel;

import com.im.lac.dataset.service.impl.DatasetHandler;
import com.im.lac.dataset.service.impl.DatasetServiceImpl;
import com.im.lac.dataset.service.impl.ImplConstants;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.SimpleRegistry;
import com.im.lac.dataset.service.impl.Utils;
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
            r.put(ImplConstants.DATASET_HANDLER, new DatasetHandler(datasetService, "/tmp/datasetcache"));
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
