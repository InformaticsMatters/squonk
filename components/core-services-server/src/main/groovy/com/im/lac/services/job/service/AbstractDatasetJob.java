package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.dataset.DataItem;
import com.im.lac.job.jobdef.ProcessDatasetJobDefinition;
import com.im.lac.services.util.Utils;
import java.util.Date;
import java.util.logging.Logger;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 * @param <T>
 */
public abstract class AbstractDatasetJob<T extends ProcessDatasetJobDefinition> extends AbstractJob<T> {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetJob.class.getName());

    private final T jobdef;

    protected DataItem result;
    protected int totalCount;
    protected int processedCount;
    protected int pendingCount;
    protected Date started;
    protected Date completed;
    protected Exception exception;

    protected AbstractDatasetJob(T jobdef) {
        super();
        this.jobdef = jobdef;
    }

    protected AbstractDatasetJob(JobStatus<T> jobStatus) {
        super(jobStatus.getJobId());
        this.jobdef = jobStatus.getJobDefinition();
        this.status = jobStatus.getStatus();
        this.started = jobStatus.getStarted();
        this.completed = jobStatus.getCompleted();
        this.processedCount = jobStatus.getProcessedCount();
        this.totalCount = jobStatus.getTotalCount();
        this.pendingCount = jobStatus.getPendingCount();
        this.result = jobStatus.getResult();
    }

    @Override
    public T getJobDefinition() {
        return jobdef;
    }

    /**
     * Updates and returns status
     *
     * @return
     */
    @Override
    public JobStatus getCurrentJobStatus() {
        return buildStatus();
    }

    @Override
    public JobStatus getUpdatedJobStatus() {
        updateStatus();
        return getCurrentJobStatus();
    }

    protected void updateStatus() {
        // noop
    }

    /**
     * Get the status based on the last updated stats. Does NOT do anything to updateStatus the stats.
     *
     * @return
     */
    public JobStatus buildStatus() {
        return new JobStatus(jobId, status, totalCount, processedCount, pendingCount, started, completed, jobdef, result);
    }

    public DataItem getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }

//    public JobStatus start(Exchange exchange) throws Exception {
//        String username = Utils.fetchUsername(exchange);
//        return start(exchange.getContext(), username);
//    }

   
}
