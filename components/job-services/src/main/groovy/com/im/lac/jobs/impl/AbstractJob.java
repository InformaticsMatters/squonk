package com.im.lac.jobs.impl;

import com.im.lac.jobs.Job;
import com.im.lac.jobs.JobStatus;
import com.im.lac.model.DataItem;
import com.im.lac.model.JobDefinition;
import com.im.lac.service.Environment;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public abstract class AbstractJob<T extends JobDefinition> implements Job {

    private static final Logger LOG = Logger.getLogger(AbstractJob.class.getName());

    protected final String jobId;
    
    protected JobStatus.Status status = JobStatus.Status.PENDING;
    protected DataItem result;
    protected int totalCount;
    protected int processedCount;
    protected int pendingCount;
    protected Date started;
    protected Date completed;
    protected T jobdef;
    protected Exception exception;

    protected AbstractJob(T jobdef) {
        jobId = UUID.randomUUID().toString();
        this.jobdef = jobdef;

    }

    protected AbstractJob(
            String jobId, 
            T jobdef,
            int totalCount,
            int processedCount) {
        this.jobId = jobId;
        this.jobdef = jobdef;;
        this.totalCount = totalCount;
        this.processedCount = processedCount;
    }

    public String getJobId() {
        return jobId;
    }

    /**
     * Updates and returns status
     *
     * @param env
     * @return
     */
    @Override
    public JobStatus getStatus(Environment env) {
        updateStatus(env);
        return buildStatus();
    }

    /**
     * Get the status based on the last updated stats. Does NOT do anything to
     * update the stats.
     *
     * @return
     */
    public JobStatus buildStatus() {
        return new JobStatus(jobId, status, totalCount, processedCount, pendingCount, started, completed, jobdef, result);
    }

    protected void updateStatus(Environment env) {
        // noop
    }

    /**
     * Executes the job. The actual execution is handled by the doExecute()
     * method which you need to override.
     *
     * @param env
     * @return The status after completion
     */
    @Override
    public JobStatus execute(Environment env) {

        started = new Date();
        status = JobStatus.Status.RUNNING;
        try {
            doExecute(env);

        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Failed to run job " + getJobId(), ex);
            status = JobStatus.Status.FAILED;
            exception = ex;
        }

        return getStatus(env);
    }

    /**
     * Do whatever is necessary to start the job.
     *
     * @param env
     * @throws java.lang.Exception
     */
    protected abstract void doExecute(Environment env) throws Exception;

}
