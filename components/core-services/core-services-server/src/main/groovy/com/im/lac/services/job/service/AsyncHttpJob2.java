package com.im.lac.services.job.service;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition2;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.IncompatibleDataException;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.services.discovery.service.ServiceDescriptorStore;
import com.im.lac.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;

/**
 * Asynchronous executor for jobs that typically take a few seconds or minutes
 * to complete.
 * <p>
 * The job remains active during execution and automatically processes the
 * results in the background. The caller should use the {@link getStatus()}
 * method to get the current status, and when it changes to
 * JobStatus.Status.COMPLETED the job is finished.</p>
 *
 * <p>
 * The job is executed along these lines:
 * <ol>
 * <li>Retrieving the dataset from the MockDatasetService</li>
 * <li>Sending the dataset to the specified HTTP service in an asynchronous
 * request-response manner</li>
 * <li>Returning to the caller as soon as the message is sent</li>
 * <li>Saving the result to the dataset service according to the specified
 * result mode.</li>
 * <li>Updating status to COMPLETE</li>
 * </ol>
 * </p>
 *
 * @author timbo
 */
public class AsyncHttpJob2 extends AbstractDatasetJob<AsyncHttpProcessDatasetJobDefinition2> {

    private static final Logger LOG = Logger.getLogger(AsyncHttpJob2.class.getName());

    /**
     *
     * @param jobdef The job definition
     */
    public AsyncHttpJob2(AsyncHttpProcessDatasetJobDefinition2 jobdef) {
        super(jobdef);
    }

    public AsyncHttpJob2(JobStatus<AsyncHttpProcessDatasetJobDefinition2> jobStatus) {
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
            converted = convertData(sd, holder);
        } catch (ClassNotFoundException | IncompatibleDataException ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to convert data to required type", ex);
        }

        InputStream results = null;
        try {
            results = postRequest(uri, converted);
            this.status = JobStatus.Status.RESULTS_READY;
        } catch (IOException ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to post request to " + uri, ex);
        }

        // handle results
        try {
            // TODO - handle metadata in smart way. All we have is JSON so we don't 
            // know about any complex datatypes. Should the service return the metadata we can use?
            Metadata metadata = new Metadata(sd.getOutputClass().getName(), sd.getOutputType(), 0);
            DataItem dataItem = saveResults(datasetHandler, results, metadata);
            this.result = dataItem;
            this.status = JobStatus.Status.COMPLETED;
        } catch (Exception ex) {
            this.status = JobStatus.Status.ERROR;
            this.exception = ex;
            LOG.log(Level.SEVERE, "Failed to save results", ex);
        }

    }

    private InputStream convertData(ServiceDescriptor sd, JsonMetadataPair holder) throws ClassNotFoundException, IncompatibleDataException {
        LOG.info("convertData()");
        Class datasetClass = Class.forName(holder.getMetadata().getClassName());
        Metadata.Type datasetType = holder.getMetadata().getType();
        // handle conversion if necessary
        if (sd.getInputClass().isAssignableFrom(datasetClass) && datasetType == sd.getInputType()) {
            return holder.getInputStream();
        } else {
            // TODO - handle conversions
            // convert from json to objects, convert types, convert to json, get InputStream 
            throw new IncompatibleDataException("Incompatible data: given: "
                    + datasetClass.getName() + "[" + datasetType + "] requires: "
                    + sd.getInputClass() + "[" + sd.getInputType() + "]"
            );
        }
    }

    private InputStream postRequest(String uri, InputStream input) throws IOException {
        LOG.info("postRequest()");
        String jsonIn = IOUtils.convertStreamToString(input, 200);
        LOG.log(Level.INFO, "JSON In: {0}", jsonIn);
        // post to destination
        String jsonOut = JobHandler.postRequestAsString(uri, jsonIn);

        LOG.info("Results returned");
        LOG.log(Level.INFO, "JSON Out: {0}", jsonOut);
        InputStream results = new ByteArrayInputStream(jsonOut.getBytes());
        return results;
    }

    private DataItem saveResults(DatasetHandler datasetHandler, InputStream results, Metadata metadata) throws Exception {
        LOG.info("saveResults()");
        // convert from json to objects
        Object objects = datasetHandler.generateObjectFromJson(results, metadata);
        LOG.log(Level.INFO, "Converted JSON to {0}", objects.getClass().getName());

        DataItem dataItem = saveResultObjects(datasetHandler, objects);
        this.processedCount = dataItem.getMetadata().getSize();
        LOG.log(Level.INFO, "processing complete. Found {0} items", this.processedCount);
        LOG.log(Level.INFO, "Metadata size: {0}", metadata.getSize());
        return dataItem;
    }

    DataItem saveResultObjects(DatasetHandler datasetHandler, Object results) throws Exception {
        LOG.info("saveResultObjects()");
        DataItem dataItem;
        switch (getJobDefinition().getMode()) {
            case UPDATE:
                dataItem = datasetHandler.updateDataset(results, getJobDefinition().getDatasetId());
                break;
            case CREATE:
                String name = getJobDefinition().getDatasetName();
                dataItem = datasetHandler.createDataset(results, name == null ? "undefined" : name);
                break;
            default:
                throw new IllegalStateException("Unexpected mode " + getJobDefinition().getMode());
        }
        return dataItem;
    }

    // for a queue job it would look like this:
    // add to jobStore
    // fetch dataset
    // create reply_to queue
    // split up job into parts
    // post each part to mqueue specifying reply_to queue
    // then (not necesarily in the same process - could be re-instated later
    // process incoming results (either using a timer or on request by the user?)
}
