package com.im.lac.job.impl;


import com.im.lac.dataset.DataItem;
import com.im.lac.job.Job;
import com.im.lac.job.JobStatus;
import com.im.lac.job.service.impl.Environment;
import com.im.lac.jobdef.DatasetJobDefinition;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public abstract class AbstractDatasetJob<T extends DatasetJobDefinition> implements Job<T> {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetJob.class.getName());

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

    protected AbstractDatasetJob(T jobdef) {
        jobId = UUID.randomUUID().toString();
        this.jobdef = jobdef;

    }

    protected AbstractDatasetJob(
            String jobId,
            T jobdef,
            int totalCount,
            int processedCount) {
        this.jobId = jobId;
        this.jobdef = jobdef;;
        this.totalCount = totalCount;
        this.processedCount = processedCount;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    public T getJobDefinition() {
        return jobdef;
    }

    /**
     * Updates and returns status
     *
     * @param env
     * @return
     */
    @Override
    public JobStatus getJobStatus(Environment env) {
        updateStatus(env);
        return buildStatus();
    }

    public JobStatus.Status getStatus() {
        return status;
    }

    public void setStatus(JobStatus.Status neu) {
        System.out.println("Updating status of job " + jobId + " to " + neu);
        this.status = neu;
    }

    /**
     * Get the status based on the last updated stats. Does NOT do anything to update the stats.
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
     * Executes the job. The actual execution is handled by the doExecute() method which you need to
     * override.
     *
     * @param env
     * @return The status after completion
     */
    @Override
    public JobStatus execute(Environment env) {

        started = new Date();
        status = JobStatus.Status.RUNNING;
        try {
            JobStatus status = doExecute(env);
            if (status != null) {
                return status;
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Failed to run job " + getJobId(), ex);
            status = JobStatus.Status.FAILED;
            exception = ex;
        }

        return buildStatus();
    }

    /**
     * Do whatever is necessary to start the job.
     *
     * @param env
     * @throws java.lang.Exception
     */
    protected abstract JobStatus doExecute(Environment env) throws Exception;

}
