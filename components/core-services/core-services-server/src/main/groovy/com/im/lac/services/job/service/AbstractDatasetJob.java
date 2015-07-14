package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.dataset.DataItem;
import com.im.lac.job.jobdef.DatasetJobDefinition;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 * @param <T>
 */
public abstract class AbstractDatasetJob<T extends DatasetJobDefinition> extends AbstractJob<T> {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetJob.class.getName());

    private final T jobdef;

    protected JobStatus.Status status = JobStatus.Status.PENDING;
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
     * Get the status based on the last updated stats. Does NOT do anything to update the stats.
     *
     * @return
     */
    public JobStatus buildStatus() {
        return new JobStatus(jobId, status, totalCount, processedCount, pendingCount, started, completed, jobdef, result);
    }

    @Override
    public JobStatus.Status getStatus() {
        return status;
    }

    public DataItem getResult() {
        return result;
    }

}
