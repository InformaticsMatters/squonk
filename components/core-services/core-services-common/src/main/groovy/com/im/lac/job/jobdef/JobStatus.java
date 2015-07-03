package com.im.lac.job.jobdef;

import com.im.lac.dataset.DataItem;
import java.util.Date;

/**
 *
 * @author timbo
 * @param <T>
 */
public class JobStatus<T extends JobDefinition> {

    public enum Status {

        PENDING, SUBMITTING, RUNNING, RESULTS_READY, COMPLETED, ERROR, FAILED, CANCELLED
    }
    private final String jobId;
    private final Status status;
    private final int totalCount;
    private final int processedCount;
    private final int pendingCount;
    private final Date started;
    private final Date completed;
    private final T jobdef;
    private final DataItem result;

    public JobStatus(
            String jobId,
            Status status,
            int totalCount,
            int processedCount,
            int pendingCount,
            Date started,
            Date completed,
            T jobdef,
            DataItem result) {
        this.jobId = jobId;
        this.status = status;
        this.totalCount = totalCount;
        this.processedCount = processedCount;
        this.pendingCount = pendingCount;
        this.started = started;
        this.completed = completed;
        this.jobdef = jobdef;
        this.result = result;
    }

    public String getJobId() {
        return jobId;
    }

    public Status getStatus() {
        return status;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getProcessedCount() {
        return processedCount;
    }
    
    public int getPendingCount() {
        return pendingCount;
    }

    public Date getStarted() {
        return started;
    }

    public Date getCompleted() {
        return completed;
    }

    public T getJobDefinition() {
        return jobdef;
    }

    public DataItem getResult() {
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("JobStatus: ").append(status)
                .append(" JobId=").append(jobId)
                .append(" TotalCount=").append(totalCount)
                .append(" ProcessedCount=").append(processedCount)
                .append(" PendingCount=").append(pendingCount)
                .append(" Job Definition=").append(jobdef);
        return b.toString();
    }

}
