package com.im.lac.jobs;

import com.im.lac.service.Environment;
import com.im.lac.service.ExecutorService;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public abstract class AbstractJob<T> implements Job {

    private static final Logger LOG = Logger.getLogger(AbstractJob.class.getName());

    protected final String jobId;
    protected final Class<T> resultType;
    protected JobStatus.Status status = JobStatus.Status.PENDING;
    protected int totalCount;
    protected int processedCount;
    protected int pendingCount;
    protected Date started;
    protected Date completed;
    protected final Object inputDatasetId;
    protected Object outputDatasetId;
    protected ExecutorService.DatasetMode mode;
    protected Exception exception;

    protected AbstractJob(Object inputDataSetId, Class resultType, ExecutorService.DatasetMode mode) {
        jobId = UUID.randomUUID().toString();
        this.inputDatasetId = inputDataSetId;
        this.resultType = resultType;
        this.mode = mode;

    }

    protected AbstractJob(
            String jobId, 
            Object inputDatasetId,
            Class resultType,
            Object outputDatasetId,
            ExecutorService.DatasetMode mode,
            int totalCount,
            int processedCount) {
        this.jobId = jobId;
        this.resultType = resultType;
        this.inputDatasetId = inputDatasetId;
        this.outputDatasetId = outputDatasetId;
        this.mode = mode;
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
        return new JobStatus(jobId, status, totalCount, processedCount, pendingCount, started, completed, inputDatasetId, outputDatasetId);
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
