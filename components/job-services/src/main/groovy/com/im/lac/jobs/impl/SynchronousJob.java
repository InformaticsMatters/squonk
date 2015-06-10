package com.im.lac.jobs.impl;

import com.im.lac.jobs.AbstractJob;
import com.im.lac.service.Environment;
import com.im.lac.jobs.JobStatus;
import com.im.lac.service.ExecutorService;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelExecutionException;

/** Job that executes synchronously (and expected to be fast!), processes the results 
 * and returns.
 * Expected that all request-response service calls would be wrapped as this type of 
 * job so that the appropriate audit and metrics are generated.
 *
 * @author timbo
 */
public class SynchronousJob<T> extends AbstractJob<T> {

    private static final Logger LOG = Logger.getLogger(SynchronousJob.class.getName());

    private final String endpoint;

    public SynchronousJob(Long inputDataSetId, Class<T> resultType, ExecutorService.DatasetMode mode, String endpoint) {
        super(inputDataSetId, resultType, mode);
        this.endpoint = endpoint;
    }

    /**
     * Executes the job synchronously and writes the result to the result set.
     *
     */
    @Override
    protected void doExecute(Environment env) throws CamelExecutionException {

        LOG.log(Level.FINE, "Sending dataset id {0} to endpoint {1}", new Object[]{inputDatasetId, endpoint});
        Object dataset = env.getDatasetService().get(inputDatasetId);
        this.totalCount = 1;
        T result = env.getExecutorService().getProducerTemplate().requestBody(endpoint, dataset, resultType);
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
        this.processedCount = 1;
        this.completed = new Date();
        this.status = JobStatus.Status.COMPLETED;
    }

}
