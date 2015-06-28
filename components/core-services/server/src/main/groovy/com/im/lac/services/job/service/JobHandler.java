package com.im.lac.services.job.service;

import com.im.lac.services.camel.Constants;
import com.im.lac.dataset.DataItem;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.job.JobStatus;
import com.im.lac.services.job.AbstractDatasetJob;
import com.im.lac.job.jobdef.DatasetJobDefinition;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class JobHandler {

    public static JobHandler getJobHandler(Exchange exch) {
        return exch.getContext().getRegistry().lookupByNameAndType(Constants.JOB_HANDLER, JobHandler.class);
    }

    public static DatasetHandler getDatasetHandler(Exchange exch) {
        return exch.getContext().getRegistry().lookupByNameAndType(Constants.DATASET_HANDLER, DatasetHandler.class);
    }

    public static JobStore getJobStore(Exchange exchange) {
        return exchange.getContext().getRegistry().lookupByNameAndType(Constants.JOB_STORE, JobStore.class);
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
        exchange.getIn().setHeader(Constants.HEADER_JOB_ID, job.getJobId());
        Object objects = datasetHandler.fetchObjectsForDataset(jobdef.getDatasetId());
        exchange.getIn().setBody(objects);
    }

    /**
     * Sets the body of the exchange as the new or updated dataset (as defined by the JobDefintion).
     * The DataItem of the new dataset is set as the body of the exchange.
     * The job status is set accordingly.
     * 
     * @param exchange
     * @throws Exception 
     */
    public static void saveDatasetForJob(Exchange exchange) throws Exception {
        DatasetHandler datasetHandler = getDatasetHandler(exchange);
        String jobId = exchange.getIn().getHeader(Constants.HEADER_JOB_ID, String.class);
        JobStore jobStore = getJobStore(exchange);
        AbstractDatasetJob job = (AbstractDatasetJob) jobStore.getJob(jobId);
        assert job != null;
        job.setStatus(JobStatus.Status.RESULTS_READY);
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
        job.setStatus(JobStatus.Status.COMPLETED);
        job.setResult(result);
        JobStatus status = job.buildStatus();
        exchange.getIn().setBody(status);
    }

}
