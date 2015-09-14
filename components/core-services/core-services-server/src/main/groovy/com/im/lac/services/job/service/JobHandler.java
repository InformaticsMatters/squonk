package com.im.lac.services.job.service;

import com.im.lac.services.ServerConstants;
import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.dataset.Metadata;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.ProcessDatasetJobDefinition;
import com.im.lac.services.CommonConstants;
import com.im.lac.services.IncompatibleDataException;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.discovery.service.ServiceDescriptorStore;
import com.im.lac.services.job.Job;
import com.im.lac.services.util.Utils;
import com.squonk.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author timbo
 */
public class JobHandler implements ServerConstants {

    private static final Logger LOG = Logger.getLogger(JobHandler.class.getName());

    public static JobHandler getJobHandler(Exchange exch) {
        return getJobHandler(exch.getContext());
    }

    public static JobHandler getJobHandler(CamelContext camelContext) {
        return camelContext.getRegistry().lookupByNameAndType(JOB_HANDLER, JobHandler.class);
    }

    public static DatasetHandler getDatasetHandler(Exchange exch) {
        return getDatasetHandler(exch.getContext());
    }

    public static DatasetHandler getDatasetHandler(CamelContext camelContext) {
        return camelContext.getRegistry().lookupByNameAndType(DATASET_HANDLER, DatasetHandler.class);
    }

    public static JobStore getJobStore(Exchange exchange) {
        return getJobStore(exchange.getContext());
    }

    public static JobStore getJobStore(CamelContext camelContext) {
        return camelContext.getRegistry().lookupByNameAndType(JOB_STORE, JobStore.class);
    }

    public static ServiceDescriptorStore getServiceDescriptorStore(Exchange exchange) {
        return getServiceDescriptorStore(exchange.getContext());
    }

    public static ServiceDescriptorStore getServiceDescriptorStore(CamelContext camelContext) {
        return camelContext.getRegistry().lookupByNameAndType(SERVICE_DESCRIPTOR_STORE, ServiceDescriptorStore.class);
    }

    /**
     * Given a AbstractDatasetJob as the current body finds the dataset specified by the job and
     * sets its contents as the body. Also sets headers {@link Constants.HEADER_JOB_ID} to the
     * appropriate property.
     *
     * @param exchange
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.NullPointerException If AbstractDatasetJob not present as Exchange body
     */
    public static void setBodyAsObjectsForDataset(Exchange exchange) throws Exception {
        DatasetHandler datasetHandler = getDatasetHandler(exchange);
        AbstractDatasetJob job = exchange.getIn().getBody(AbstractDatasetJob.class);
        if (job == null) {
            throw new NullPointerException("No AbstractDatasetJob found as body");
        }
        ProcessDatasetJobDefinition jobdef = job.getJobDefinition();
        exchange.getIn().setHeader(REST_JOB_ID, job.getJobId());
        Object objects = datasetHandler.fetchObjectsForDataset(Utils.fetchUsername(exchange), jobdef.getDatasetId());
        exchange.getIn().setBody(objects);
    }

    /**
     * Sets the body of the exchange as the new or updated dataset (as defined by the JobDefintion).
     * The DataItem of the new dataset is set as the body of the exchange. The job status is set
     * accordingly.
     *
     * @param exchange
     * @throws Exception
     */
    public static void saveDatasetForJob(Exchange exchange) throws Exception {
        DatasetHandler datasetHandler = getDatasetHandler(exchange);
        String username = exchange.getIn().getHeader(CommonConstants.HEADER_SQUONK_USERNAME, String.class);
        if (username == null) {
            throw new IllegalStateException("Validated username not specified");
        }
        String jobId = exchange.getIn().getHeader(REST_JOB_ID, String.class);
        JobStore jobStore = getJobStore(exchange);
        AbstractDatasetJob job = (AbstractDatasetJob) jobStore.getJob(jobId);
        LOG.log(Level.FINE, "Saving dataset for job {0}", jobId);
        job.status = JobStatus.Status.RESULTS_READY;
        Object results = exchange.getIn().getBody();
        DataItem result;
        switch (job.getJobDefinition().getDatasetMode()) {
            case UPDATE:
                result = datasetHandler.updateDataset(username, results, job.getJobDefinition().getDatasetId());
                break;
            case CREATE:
                String name = job.getJobDefinition().getDatasetName();
                result = datasetHandler.createDataset(username, results, name == null ? "undefined" : name);
                break;
            default:
                throw new IllegalStateException("Unexpected mode " + job.getJobDefinition().getDatasetMode());
        }
        LOG.log(Level.FINE, "saving data complete. Result: {0}", result);
        job.status = JobStatus.Status.COMPLETED;
        job.result = result;
        JobStatus status = job.buildStatus();
        exchange.getIn().setBody(status);
    }

    public static void putJobStatuses(Exchange exchange) throws Exception {
        JobStore store = getJobStore(exchange);
        List<JobStatus> results = getJobStatuses(store);
        exchange.getIn().setBody(results);
    }

    protected static List<JobStatus> getJobStatuses(JobStore store) {
        List<JobStatus> results = new ArrayList<>();
        Date now = new Date();
        for (Job job : store.getJobs()) {
            JobStatus status = job.getCurrentJobStatus();
            // TODO - this is a temp measure to purge jobs older than 15mins
            // needs to be replaced by proper job management and query
            Date started = status.getStarted();
            LOG.log(Level.FINE, "Job {0} started: {1} now: {2}", new Object[]{job.getJobId(), started, now});
            if (started != null && (now.getTime() - started.getTime() > 15 * 60 * 1000)) {
                LOG.log(Level.INFO, "Purging job {0}", job.getJobId());
                store.removeJob(job.getJobId());
            } else {
                //LOG.log(Level.FINE, "Adding job {0}", job.getJobId());
                results.add(job.getCurrentJobStatus());
            }
        }
        return results;
    }

    public static void setJobStatus(Exchange exchange, JobStatus.Status status) {
        AbstractDatasetJob job = JobHandler.getJobFromHeader(exchange, AbstractDatasetJob.class);
        if (job != null) {
            job.status = status;
        }
    }

    /**
     * Put the current job status to the body of the exchange
     *
     * @param exchange
     */
    public static void putCurrentJobStatus(Exchange exchange) {
        Job job = getJobFromHeader(exchange);
        if (job == null) {
            exchange.getIn().setBody(null);
        } else {
            exchange.getIn().setBody(job.getCurrentJobStatus());
        }
    }

    /**
     * Update the job status and set it as the body of the exchange
     *
     * @param exchange
     */
    public static void putUpdatedJobStatus(Exchange exchange) {
        Job job = getJobFromHeader(exchange);
        if (job == null) {
            exchange.getIn().setBody(null);
        } else {
            exchange.getIn().setBody(job.getUpdatedJobStatus());
        }
    }

    public static Job getJob(CamelContext context, String jobId) {
        JobStore store = getJobStore(context);
        return store.getJob(jobId);
    }

    public static Job getJobFromHeader(Exchange exchange) {
        JobStore store = getJobStore(exchange);
        return store.getJob(exchange.getIn().getHeader(REST_JOB_ID, String.class));
    }

    public static <T> T getJobFromHeader(Exchange exchange, Class<T> type) {
        JobStore store = getJobStore(exchange);
        return (T) store.getJob(exchange.getIn().getHeader(REST_JOB_ID, String.class));
    }

    public static InputStream convertData(ServiceDescriptor sd, JsonMetadataPair holder) throws ClassNotFoundException, IncompatibleDataException {
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

    public static InputStream postRequest(String uri, InputStream content) throws IOException {
        // TODO - handle using InputStream but HttpComponents seems to screw up here
        // so converting to String as temp measure
        LOG.log(Level.INFO, "POSTing to {0}", uri);
        String jsonIn = IOUtils.convertStreamToString(content, 200);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(new StringEntity(jsonIn));
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            LOG.info(response.getStatusLine().toString());
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("HTTP POST failed: " + response.getStatusLine().toString());
            }
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            //LOG.log(Level.INFO, "JSON HTTP: {0}", json);
            return new ByteArrayInputStream(json.getBytes());
        }
    }

    public static DataItem createResultsWithInputStream(String username, DatasetHandler datasetHandler, InputStream results, Metadata metadata, String datasetName)
            throws Exception {
        LOG.info("createResultsWithInputStream()");
        // convert from json to objects
        Object objects = datasetHandler.generateObjectFromJson(results, metadata);
        return createResultsWithObjects(username, datasetHandler, objects, datasetName);
    }

    public static DataItem createResultsWithObjects(String username, DatasetHandler datasetHandler, Object objects, String datasetName)
            throws Exception {
        LOG.info("createResultsWithObjects()");

        DataItem dataItem = datasetHandler.createDataset(username, objects, datasetName == null ? "undefined" : datasetName);
        return dataItem;
    }

    /**
     * Update the dataset with the content (probably json) contained in the InputStream
     *
     * @param username
     * @param datasetHandler
     * @param results
     * @param metadata
     * @param datasetId
     * @return
     * @throws Exception
     */
    public static DataItem updateResultsWithInputStream(String username, DatasetHandler datasetHandler, InputStream results, Metadata metadata, Long datasetId)
            throws Exception {
        LOG.info("updateResultsWithInputStream()");
        // convert from json to objects
        Object objects = datasetHandler.generateObjectFromJson(results, metadata);
        return updateResultsWithObjects(username, datasetHandler, objects, datasetId);
    }

    public static DataItem updateResultsWithObjects(String username, DatasetHandler datasetHandler, Object objects, Long datasetId)
            throws Exception {
        LOG.info("updateResultsWithObjects()");
        DataItem dataItem = datasetHandler.updateDataset(username, objects, datasetId);
        return dataItem;
    }

}
