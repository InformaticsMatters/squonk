package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.job.service.adapters.HttpGenericParamsJobAdapter;
import java.util.logging.Logger;

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
 * <li>Perform some initial validation of the job
 * <li>Start the job is a different thread and returning to the caller with the status set to
 * RUNNING</li>
 * <li>Retrieving the dataset from the Dataset Service</li>
 * <li>Sending the dataset to the specified HTTP service in an asynchronous request-response manner
 * e.g. a REST web service call</li>
 * <li>Saving the result to the dataset service according to the specified result mode.</li>
 * <li>Updating status to COMPLETE</li>
 * </ol>
 * </p>
 *
 * @author timbo
 */
public class AsyncHttpJob extends AbstractDatasetServiceJob<AsyncHttpProcessDatasetJobDefinition> {

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

    @Override
    protected Class getDefaultAdapterClass() {
        return HttpGenericParamsJobAdapter.class;
    }

}
