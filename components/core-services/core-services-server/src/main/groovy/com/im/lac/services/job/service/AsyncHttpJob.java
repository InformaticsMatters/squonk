package com.im.lac.services.job.service;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.IncompatibleDataException;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.services.discovery.service.ServiceDescriptorStore;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;

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
 * <li>Retrieving the dataset from the MockDatasetService</li>
 * <li>Sending the dataset to the specified HTTP service in an asynchronous request-response
 * manner</li>
 * <li>Returning to the caller as soon as the message is sent</li>
 * <li>Saving the result to the dataset service according to the specified result mode.</li>
 * <li>Updating status to COMPLETE</li>
 * </ol>
 * </p>
 *
 * @author timbo
 */
public class AsyncHttpJob extends AbstractDatasetJob<AsyncHttpProcessDatasetJobDefinition> {

    private static final Logger LOG = Logger.getLogger(AsyncHttpJob.class.getName());

    /**
     *
     * @param jobdef The job definition
     */
    public AsyncHttpJob(AsyncHttpProcessDatasetJobDefinition jobdef) {
        super(jobdef);
    }

    public AsyncHttpJob(JobStatus<AsyncHttpProcessDatasetJobDefinition> jobStatus) {
        super(jobStatus);
    }

    public JobStatus start(CamelContext context) throws Exception {
        JobStore jobStore = JobHandler.getJobStore(context);
        DatasetHandler datasetHandler = JobHandler.getDatasetHandler(context);
        ServiceDescriptorStore serviceDescriptorStore = JobHandler.getServiceDescriptorStore(context);

        return start(jobStore, datasetHandler, serviceDescriptorStore);
    }

    public JobStatus start(
            JobStore jobStore,
            DatasetHandler datasetHandler,
            ServiceDescriptorStore serviceDescriptorStore)
            throws Exception {
        LOG.info("start()");
        // add to jobStore
        jobStore.putJob(this);

        String serviceId = getJobDefinition().getServiceId();
        String accessModeId = getJobDefinition().getAccessModeId();

        ServiceDescriptor sd = serviceDescriptorStore.getServiceDescriptor(serviceId);
        LOG.log(Level.INFO, "ServiceDescriptor: {0}", sd);
        if (sd == null) {
            throw new IllegalStateException("Service " + serviceId + " cannot be found");
        }
        String uri = serviceDescriptorStore.resolveEndpoint(serviceId, accessModeId);
        if (uri == null) {
            this.status = JobStatus.Status.ERROR;
            this.exception = new NullPointerException("Service endpoint could not be resolved. Check the service configuration.");
            return getCurrentJobStatus();
        }

        Thread t = new Thread() {
            @Override
            public void run() {

                executeJob(datasetHandler, sd, uri);
            }
        };
        this.status = JobStatus.Status.RUNNING;

        JobStatus st = getCurrentJobStatus();
        t.start();
        return st;
    }

    void executeJob(DatasetHandler datasetHandler, ServiceDescriptor sd, String uri) {
        LOG.info("executeJob()");
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
        }

        InputStream converted = null;
        try {
            // next step is to convert
            converted = JobHandler.convertData(sd, holder);
        } catch (Exception ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to convert data to required type", ex);
        }

        InputStream results = null;
        try {
            results = JobHandler.postRequest(uri, converted);
            this.status = JobStatus.Status.RESULTS_READY;
        } catch (Exception ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to post request to " + uri, ex);
        }

        // handle results
        try {
            // TODO - handle metadata in smart way. All we have is JSON so we don't 
            // know about any complex datatypes. Should the service return the metadata we can use?
            Metadata metadata = new Metadata(sd.getOutputClass().getName(), sd.getOutputType(), 0);
            DataItem dataItem;
            switch (getJobDefinition().getDatasetMode()) {
                case UPDATE:
                    dataItem = JobHandler.updateResultsFrom(datasetHandler, results, metadata, getJobDefinition().getDatasetId());
                    break;
                case CREATE:
                    dataItem = JobHandler.createResults(datasetHandler, results, metadata, getJobDefinition().getDatasetName());
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

}
