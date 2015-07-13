package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
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
 * <li>Retrieving the dataset from the MockDatasetService</li>
 * <li>Sending the dataset to the specified HTTP service in an asynchronous request-response manner</li>
 * <li>Returning to the caller as soon as the message is sent</li>
 * <li>Saving the result to the dataset service according to the specified result mode.</li>
 * <li>Updating status to COMPLETE</li>
 * </ol>
 * </p>
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

}
