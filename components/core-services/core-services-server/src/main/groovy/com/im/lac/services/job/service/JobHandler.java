package com.im.lac.services.job.service;

import com.im.lac.services.ServerConstants;
import com.im.lac.dataset.DataItem;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.DatasetJobDefinition;
import com.im.lac.services.CommonConstants;
import com.im.lac.services.job.Job;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class JobHandler {
    
    private static final Logger LOG = Logger.getLogger(JobHandler.class.getName());
    
    public static JobHandler getJobHandler(Exchange exch) {
        return getJobHandler(exch.getContext());
    }
    
    public static JobHandler getJobHandler(CamelContext camelContext) {
        return camelContext.getRegistry().lookupByNameAndType(ServerConstants.JOB_HANDLER, JobHandler.class);
    }
    
    public static DatasetHandler getDatasetHandler(Exchange exch) {
        return getDatasetHandler(exch.getContext());
    }
    
    public static DatasetHandler getDatasetHandler(CamelContext camelContext) {
        return camelContext.getRegistry().lookupByNameAndType(ServerConstants.DATASET_HANDLER, DatasetHandler.class);
    }
    
    public static JobStore getJobStore(Exchange exchange) {
        return getJobStore(exchange.getContext());
    }
    
    public static JobStore getJobStore(CamelContext camelContext) {
        return camelContext.getRegistry().lookupByNameAndType(ServerConstants.JOB_STORE, JobStore.class);
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
        DatasetJobDefinition jobdef = job.getJobDefinition();
        exchange.getIn().setHeader(ServerConstants.HEADER_JOB_ID, job.getJobId());
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
        String jobId = exchange.getIn().getHeader(ServerConstants.HEADER_JOB_ID, String.class);
        JobStore jobStore = getJobStore(exchange);
        AbstractDatasetJob job = (AbstractDatasetJob) jobStore.getJob(jobId);
        LOG.log(Level.FINE, "Saving dataset for job {0}", jobId);
        job.status = JobStatus.Status.RESULTS_READY;
        Object results = exchange.getIn().getBody();
        DataItem result;
        switch (job.getJobDefinition().getMode()) {
            case UPDATE:
                result = datasetHandler.updateDataset(results, job.getJobDefinition().getDatasetId());
                break;
            case CREATE:
                String name = job.getJobDefinition().getDatasetName();
                result = datasetHandler.createDataset(results, name == null ? "undefined" : name);
                break;
            default:
                throw new IllegalStateException("Unexpected mode " + job.getJobDefinition().getMode());
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
        AbstractDatasetJob job = getJob(exchange, AbstractDatasetJob.class);
        job.status = status;
    }

    /**
     * Put the current job status to the body of the exchange
     *
     * @param exchange
     */
    public static void putCurrentJobStatus(Exchange exchange) {
        Job job = getJob(exchange);
        exchange.getIn().setBody(job.getCurrentJobStatus());
    }

    /**
     * Update the job status and set it as the body of the exchange
     *
     * @param exchange
     */
    public static void putUpdatedJobStatus(Exchange exchange) {
        Job job = getJob(exchange);
        exchange.getIn().setBody(job.getUpdatedJobStatus());
    }
    
    public static Job getJob(CamelContext context, String jobId) {
        JobStore store = getJobStore(context);
        return store.getJob(jobId);
    }
    
    public static Job getJob(Exchange exchange) {
        String jobId = exchange.getIn().getHeader(CommonConstants.HEADER_JOB_ID, String.class);
        JobStore store = getJobStore(exchange);
        return store.getJob(jobId);
    }
    
    public static <T> T getJob(Exchange exchange, Class<T> type) {
        String jobId = exchange.getIn().getHeader(CommonConstants.HEADER_JOB_ID, String.class);
        JobStore store = getJobStore(exchange);
        return (T) store.getJob(jobId);
    }
    
}
