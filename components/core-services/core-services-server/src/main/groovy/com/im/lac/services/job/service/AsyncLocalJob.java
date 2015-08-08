package com.im.lac.services.job.service;

import com.im.lac.dataset.DataItem;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.job.jobdef.AsyncLocalProcessDatasetJobDefinition;
import com.im.lac.services.discovery.service.ServiceDescriptorStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

/**
 * Asynchronous executor for jobs that typically take a few seconds or minutes to complete.
 * <p>
 * The job remains active during execution and automatically processes the results in the
 * background. The caller should use the {@link getStatus()} method to get the current status, and
 * when it changes to JobStatus.Status.COMPLETED the job is finished.</p>
 *
 * <p>
 * The job is executed along these lines:
 * <ol>
 * <li>Retrieving the dataset from the DatasetService</li>
 * <li>Sending the dataset to the specified Camel route service in an asynchronous request-response
 * manner</li>
 * <li>Returning to the caller as soon as the message is sent</li>
 * <li>Saving the result to the dataset service according to the specified result mode.</li>
 * <li>Updating status to COMPLETE</li>
 * </ol>
 * </p>
 *
 * @author timbo
 */
public class AsyncLocalJob extends AbstractDatasetJob<AsyncLocalProcessDatasetJobDefinition> {

    private static final Logger LOG = Logger.getLogger(AsyncLocalJob.class.getName());

    /**
     *
     * @param jobdef The job definition
     */
    public AsyncLocalJob(AsyncLocalProcessDatasetJobDefinition jobdef) {
        super(jobdef);
    }

    public AsyncLocalJob(JobStatus<AsyncLocalProcessDatasetJobDefinition> jobStatus) {
        super(jobStatus);
    }

    public JobStatus start(CamelContext context) throws Exception {
        LOG.info("start()");

        JobStore jobStore = JobHandler.getJobStore(context);
        DatasetHandler datasetHandler = JobHandler.getDatasetHandler(context);
        ServiceDescriptorStore serviceDescriptorStore = JobHandler.getServiceDescriptorStore(context);

        // add to jobStore
        jobStore.putJob(this);

        String serviceId = getJobDefinition().getServiceId();
        String accessModeId = getJobDefinition().getAccessModeId();

        ServiceDescriptor sd = serviceDescriptorStore.getServiceDescriptor(serviceId);
        LOG.log(Level.INFO, "ServiceDescriptor: {0}", sd);
        if (sd == null) {
            throw new IllegalStateException("Service " + serviceId + " cannot be found");
        }
        String endpoint = serviceDescriptorStore.getEndpoint(serviceId, accessModeId);

        Thread t = new Thread() {
            @Override
            public void run() {

                executeJob(context, datasetHandler, sd, endpoint);
            }
        };
        this.status = JobStatus.Status.RUNNING;
        JobStatus st = getCurrentJobStatus();
        t.start();
        return st;
    }

    void executeJob(CamelContext context, DatasetHandler datasetHandler, ServiceDescriptor sd, String endpoint) {
        LOG.log(Level.INFO, "executeJob() {0}", endpoint);
        JsonMetadataPair holder = null;
        try {
            // fetch dataset
            holder = datasetHandler.fetchJsonForDataset(getJobDefinition().getDatasetId());
            LOG.log(Level.INFO, "Retrieved dataset: {0}", holder.getMetadata());
            this.totalCount = holder.getMetadata().getSize();
            LOG.log(Level.INFO, "data fetched. Found {0} items", this.totalCount);

        } catch (Exception ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to fetch dataset", ex);
            return;
        }

        Object results = null;
        try {
            //convert from json to objects
            Object objects = datasetHandler.generateObjectFromJson(holder.getInputStream(), holder.getMetadata());

            // we don't worry about converstions as these types of jobs are jsut for test purposes
            ProducerTemplate pt = context.createProducerTemplate();
            // TODO - handle params
            results = pt.requestBody(endpoint, objects);
            this.status = JobStatus.Status.RESULTS_READY;
        } catch (Exception ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to post request to " + endpoint, ex);
            return;
        }

        // handle results
        try {
            DataItem dataItem;
            switch (getJobDefinition().getDatasetMode()) {
                case UPDATE:
                    dataItem = datasetHandler.updateDataset(results, getJobDefinition().getDatasetId());
                    break;
                case CREATE:
                    String datasetName = getJobDefinition().getDatasetName();
                    dataItem = datasetHandler.createDataset(results, datasetName == null ? "undefined" : datasetName);
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
            return;
        }

    }

}
