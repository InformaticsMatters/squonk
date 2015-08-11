package com.im.lac.services.job.service;

import com.im.lac.services.ServerConstants;
import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.dataset.Metadata;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.ProcessDatasetJobDefinition;
import com.im.lac.services.IncompatibleDataException;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.discovery.service.ServiceDescriptorStore;
import com.im.lac.services.job.Job;
import com.im.lac.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
        Object objects = datasetHandler.fetchObjectsForDataset(jobdef.getDatasetId());
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
        String jobId = exchange.getIn().getHeader(REST_JOB_ID, String.class);
        JobStore jobStore = getJobStore(exchange);
        AbstractDatasetJob job = (AbstractDatasetJob) jobStore.getJob(jobId);
        LOG.log(Level.FINE, "Saving dataset for job {0}", jobId);
        job.status = JobStatus.Status.RESULTS_READY;
        Object results = exchange.getIn().getBody();
        DataItem result;
        switch (job.getJobDefinition().getDatasetMode()) {
            case UPDATE:
                result = datasetHandler.updateDataset(results, job.getJobDefinition().getDatasetId());
                break;
            case CREATE:
                String name = job.getJobDefinition().getDatasetName();
                result = datasetHandler.createDataset(results, name == null ? "undefined" : name);
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
        List<JobStatus> results = new ArrayList<>();
        for (Job job : store.getJobs()) {
            results.add(job.getCurrentJobStatus());
        }
        exchange.getIn().setBody(results);
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

    public static DataItem createResults(DatasetHandler datasetHandler, InputStream results, Metadata metadata, String datasetName)
            throws Exception {
        LOG.info("saveResults()");
        // convert from json to objects
        Object objects = datasetHandler.generateObjectFromJson(results, metadata);
        LOG.log(Level.INFO, "Converted JSON to {0}", objects.getClass().getName());

        DataItem dataItem = datasetHandler.createDataset(results, datasetName == null ? "undefined" : datasetName);

        LOG.log(Level.INFO, "Metadata size: {0}", metadata.getSize());
        return dataItem;
    }
    
    public static DataItem updateResultsFrom(DatasetHandler datasetHandler, InputStream results, Metadata metadata, Long datasetId)
            throws Exception {
        LOG.info("saveResults()");
        // convert from json to objects
        Object objects = datasetHandler.generateObjectFromJson(results, metadata);
        DataItem dataItem = datasetHandler.updateDataset(objects, datasetId);
        LOG.log(Level.INFO, "Metadata size: {0}", metadata.getSize());
        return dataItem;
    }


}
