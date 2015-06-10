package com.im.lac.jobs.impl;

import com.im.lac.jobs.AbstractJob;
import com.im.lac.service.Environment;
import com.im.lac.jobs.JobStatus;
import com.im.lac.service.ExecutorService;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.spi.Synchronization;

/**
 * Asynchronous executor for jobs that typically take a few seconds or minutes to
 * complete.
 * <p>The job remains active during execution and automatically processes
 * the results in the background. 
 * The caller should use the {@link getStatus()} method to get the current status, 
 * and when it changes to JobStatus.Status.COMPLETED the job is finished.</p>
 * 
 * <p>The job is executed by the {@link doExecute()} method along these lines:
 * <ol>
 * <li>Retrieving the dataset from the DatasetService</li>
 * <li>Sending the dataset to the specified JMS queue in an asynchronous request-response manner</li>
 * <li>Returning to the caller as soon as the message is sent</li>
 * <li>Waiting for the result on the temporary queue generated for the response</li>
 * <li>Saving the result to the dataset service according to the specified result mode.</li>
 * <li>Updating status to COMPLETE</li> 
 * </ol>
 * </p>
 * <p>
 * The job is processed by some consumer that is listening to the JMS queue and posting 
 * the result to the temporary queue specified by the JmsReplyTo header. Except for
 * the name of the queue to post the data to this is all transparent to the job definition.</p>
 * 
 * @author timbo
 */
public class AsynchronousJob<T> extends AbstractJob<T> {

    private static final Logger LOG = Logger.getLogger(AsynchronousJob.class.getName());

    private final String queueName;

    /**
     * 
     * @param env 
     * @param inputDataSetId The input dataset ID
     * @param resultType The class of the result
     * @param mode The result mode
     * @param queueName The JMS queue name to post the result to.
     */
    public AsynchronousJob(Object inputDataSetId, Class<T> resultType, ExecutorService.DatasetMode mode, String queueName) {
        super(inputDataSetId, resultType, mode);
        this.queueName = queueName;
    }

    public String getRequestQueueUri() {
        return ExecutorService.JMS_BROKER_NAME + ":queue:" + queueName;
    }

    @Override
    public void doExecute(Environment env) throws CamelExecutionException {
        LOG.log(Level.FINE, "QueueJob.execute() Sending dataset id {0} to queue {1}", new Object[]{inputDatasetId, queueName});
        Object dataset = env.getDatasetService().get(inputDatasetId);
        this.totalCount = 1;
        Future<Exchange> results = env.getExecutorService().getProducerTemplate().asyncCallback(
                getRequestQueueUri(),
                (Exchange exchange) -> {
                    exchange.getIn().setHeader("JobId", getJobId());
                    exchange.getIn().setBody(dataset);
                },
                new Synchronization() {

                    @Override
                    public void onComplete(Exchange exchng) {
                        LOG.info("Job request completed");
                        status = JobStatus.Status.RESULTS_READY;
                        T body = exchng.getIn().getBody(resultType);
                        handleResults(env, body);
                        
                    }

                    @Override
                    public void onFailure(Exchange exchng) {
                        LOG.info("Job request failed");
                        status = JobStatus.Status.FAILED;
                    }
                });
    }

    protected void handleResults(Environment env, T result) {
        this.pendingCount = 1;
        switch (mode) {
            case UPDATE:
                env.getDatasetService().update(inputDatasetId, result);
                this.outputDatasetId = inputDatasetId;
                break;
            case CREATE:
                Object newId = env.getDatasetService().put(result);
                this.outputDatasetId = newId;
                break;
            default:
                throw new IllegalStateException("Unexpected mode " + mode);
        }
        this.pendingCount = 0;
        this.processedCount = 1;
        this.completed = new Date();
        this.status = JobStatus.Status.COMPLETED;
    }

}