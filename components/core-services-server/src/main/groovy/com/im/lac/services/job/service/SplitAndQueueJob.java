package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.AbstractProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.SplitAndQueueProcessDatasetJobDefinition;
import java.util.logging.Logger;

/**
 * Queue based executor for slow jobs that can be split into parts and submitted to a queue for
 * execution.
 * <p>
 * Each queue would have a corresponding consumer that takes from the queue, processes the item and
 * sends the response (if any) to a second queue that is dedicated for the results of this job. The
 * service consuming the request queue can be auto-scaled as needed to increase throughput
 * (auto-scaling on the number of unprocessed messages).</p>
 *
 * <p>
 * Assumptions:
 * <ol>
 * <li>the submitted messages have fields for Job ID, response queue name</li>
 * <li>the submitted message bodies are JSON and the consuming service knows what class to
 * instantiate from that JSON</li>
 * <li>the response bodies are JSON and need no further processing other than being written as a new
 * dataset as an Array JSON objects (note: potentially we could add a post-processing step if this
 * is too restrictive?)
 * </ol>
 * </p>
 * <p>
 * The process for execution is:
 * <ol>
 * <li>retrieve dataset</li>
 * <li>split dataset - assume it's a Collection, Stream or similar - something that we know how to
 * split</li>
 * <li>create a new queue for the response (dedicated to this job)</li>
 * <li>submit each item to request queue, specifying the queue name for the responses</li>
 * <li>monitor items from response queue</li>
 * <li>create/updateStatus dataset when requested</li>
 * <li>when response queue has expected totalCount then updateStatus dataset with final results</li>
 * <li>delete the response queue</li>
 * </ol>
 * </p>
 *
 * @author timbo
 */
public class SplitAndQueueJob extends AbstractDatasetJob<SplitAndQueueProcessDatasetJobDefinition> {

    private static final Logger LOG = Logger.getLogger(SplitAndQueueJob.class.getName());
    private static final String RESPONSE_QUEUE_PREFIX = "QueueJob_";

    /**
     *
     * @param jobdef The job definition
     */
    public SplitAndQueueJob(SplitAndQueueProcessDatasetJobDefinition jobdef) {
        super(jobdef);
    }

    protected String getResponseQueueName() {
        return RESPONSE_QUEUE_PREFIX + getJobId();
    }
    
    
}
