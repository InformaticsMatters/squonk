package com.im.lac.jobs.impl;

import static com.im.lac.jobs.impl.CamelExecutor.DATASET_HANDLER;
import static com.im.lac.jobs.impl.CamelExecutor.JMS_BROKER_NAME;
import static com.im.lac.jobs.impl.CamelExecutor.JOB_STORE;
import com.im.lac.service.DatasetService;
import com.im.lac.service.impl.SimpleJobStore;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.SimpleRegistry;
import com.im.lac.service.impl.Utils;
import java.util.logging.Logger;

import javax.sql.DataSource;
import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;

/**
 *
 * @author timbo
 */
public class CamelLifeCycle {
    
    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());
    
    private final String brokerUri;
    private final DataSource dataSource;
    
    public CamelLifeCycle() {
        this.brokerUri = "vm://localhost?broker.persistent=false";
        this.dataSource = Utils.createDataSource();
        
    }
    
    public CamelLifeCycle(String brokerUri, DataSource dataSource) {
        this.brokerUri = brokerUri;
        this.dataSource = dataSource;
    }
    
    public void beforeStart(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.info("Creating DatasetService");
        DatasetService datasetService = new DatasetService(dataSource);
        datasetService.createTables();
        
        LOG.info("Creating SimpleJobStore");
        r.put(JOB_STORE, new SimpleJobStore());
        
        LOG.info("Creating DatasetHandler");
        r.put(DATASET_HANDLER, new DatasetHandler(datasetService, "/tmp/datasetcache"));
        
        context.addComponent(JMS_BROKER_NAME, activeMQComponent(brokerUri));
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
