package com.im.lac.services.job.service;

import com.im.lac.services.job.service.adapters.ProcessDatasetJobAdapter;
import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.job.JobExecutionException;
import com.im.lac.job.jobdef.AbstractAsyncProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.ServiceExecutionJobDefinition;
import com.im.lac.services.AccessMode;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.discovery.service.ServiceDescriptorStore;
import com.im.lac.services.discovery.service.ServiceDescriptorUtils;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 * @param <T>
 */
public abstract class AbstractDatasetServiceJob<T extends AbstractAsyncProcessDatasetJobDefinition> extends AbstractDatasetJob {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetServiceJob.class.getName());

    protected AbstractDatasetServiceJob(T jobdef) {
        super(jobdef);
    }

    protected AbstractDatasetServiceJob(JobStatus<T> jobStatus) {
        super(jobStatus);
    }

    @Override
    public JobStatus start(CamelContext context, String username) throws Exception {
        LOG.info("start()");
        JobStore jobStore = JobHandler.getJobStore(context);
        DatasetHandler datasetHandler = JobHandler.getDatasetHandler(context);
        ServiceDescriptorStore serviceDescriptorStore = JobHandler.getServiceDescriptorStore(context);

        // add to jobStore
        jobStore.putJob(this);

        String serviceId = ((ServiceExecutionJobDefinition) getJobDefinition()).getServiceId();
        String accessModeId = ((ServiceExecutionJobDefinition) getJobDefinition()).getAccessModeId();

        ServiceDescriptor sd = serviceDescriptorStore.getServiceDescriptor(serviceId);
        AccessMode accessMode = ServiceDescriptorUtils.findAccessMode(sd, accessModeId);

        LOG.log(Level.INFO, "ServiceDescriptor: {0}", sd);
        if (sd == null) {
            throw new IllegalStateException("Service " + serviceId + " cannot be found");
        }
        String uri = serviceDescriptorStore.resolveEndpoint(serviceId, accessMode);
        if (uri == null) {
            this.status = JobStatus.Status.ERROR;
            this.exception = new NullPointerException("Service endpoint could not be resolved. Check the service configuration.");
            return getCurrentJobStatus();
        }

        Thread t = new Thread() {
            @Override
            public void run() {

                executeJob(context, username, datasetHandler, sd, accessMode, uri);
            }
        };
        this.status = JobStatus.Status.RUNNING;

        JobStatus st = getCurrentJobStatus();
        t.start();
        return st;
    }

    void executeJob(CamelContext context, String username, DatasetHandler datasetHandler, ServiceDescriptor sd, AccessMode accessMode, String endpoint) {
        LOG.log(Level.INFO, "executeJob() {0}", endpoint);
        JsonMetadataPair holder = null;
        try {
            // fetch dataset
            holder = datasetHandler.fetchJsonForDataset(username, getJobDefinition().getDatasetId());
            LOG.log(Level.INFO, "Retrieved dataset: {0}", holder.getMetadata());
            this.totalCount = holder.getMetadata().getSize();
            LOG.log(Level.INFO, "data fetched. Found {0} items", this.totalCount);

        } catch (Exception ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to fetch dataset", ex);
            return;
        }

        Object objects;
        try {
            objects = processData(context, sd, accessMode, endpoint, holder);
            this.status = JobStatus.Status.RESULTS_READY;
        } catch (JobExecutionException ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to execute service request to " + endpoint, ex);
            return;
        }

        // handle results
        try {
            DataItem dataItem;
            switch (getJobDefinition().getDatasetMode()) {
                case UPDATE:
                    dataItem = JobHandler.updateResultsWithObjects(username, datasetHandler, objects, getJobDefinition().getDatasetId());
                    break;
                case CREATE:
                    dataItem = JobHandler.createResultsWithObjects(username, datasetHandler, objects, getJobDefinition().getDatasetName());
                    break;
                default:
                    throw new IllegalStateException("Unexpected mode " + getJobDefinition().getDatasetMode());
            }

            this.processedCount = dataItem.getMetadata().getSize();
            this.result = dataItem;
            this.status = JobStatus.Status.COMPLETED;
        } catch (Exception ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to save results", ex);
        }
    }

    Object processData(CamelContext context, ServiceDescriptor sd, AccessMode accessMode, String endpoint, JsonMetadataPair holder)
            throws JobExecutionException {

        try {
            ProcessDatasetJobAdapter adpater = createAdapter(sd, accessMode);
            Map<String,Object> params = ((AbstractAsyncProcessDatasetJobDefinition)getJobDefinition()).getParameters();
            return adpater.process(context, this, sd, endpoint, holder, params);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new JobExecutionException("Failed to process data", ex);
        }
    }

    ProcessDatasetJobAdapter createAdapter(ServiceDescriptor sd, AccessMode accessMode) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String clsname = accessMode.getAdapterClassName();
        Class adapterClass;
        if (clsname != null) {
            adapterClass = Class.forName(clsname);
            LOG.log(Level.INFO, "Using adapter of type {0}", adapterClass.getName());
        } else {
            adapterClass = getDefaultAdapterClass();
            LOG.log(Level.INFO, "No adapter defined. using default of {0}", adapterClass.getName());
        }
        return (ProcessDatasetJobAdapter) adapterClass.newInstance();
    }

    abstract protected Class getDefaultAdapterClass();
}
