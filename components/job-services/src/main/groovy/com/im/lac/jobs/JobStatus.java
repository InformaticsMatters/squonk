package com.im.lac.jobs;

import java.util.Date;

/**
 *
 * @author timbo
 */
public class JobStatus {

    public enum Status {

        PENDING, RUNNING, RESULTS_READY, COMPLETED, ERROR, FAILED, CANCELLED
    }
    private final String jobId;
    private final Status status;
    private final int totalCount;
    private final int processedCount;
    private final int pendingCount;
    private final Date started;
    private final Date completed;
    private final Object inputDatasetId;
    private final Object outputDatasetId;

    public JobStatus(
            String jobId,
            Status status,
            int totalCount,
            int processedCount,
            int pendingCount,
            Date started,
            Date completed,
            Object inputDatasetId,
            Object outputDatasetId) {
        this.jobId = jobId;
        this.status = status;
        this.totalCount = totalCount;
        this.processedCount = processedCount;
        this.pendingCount = pendingCount;
        this.started = started;
        this.completed = completed;
        this.inputDatasetId = inputDatasetId;
        this.outputDatasetId = outputDatasetId;
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

    public Object getInputDatasetId() {
        return inputDatasetId;
    }

    public Object getOutputDatasetId() {
        return outputDatasetId;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("JobStatus: ").append(status)
                .append(" JobId=").append(jobId)
                .append(" TotalCount=").append(totalCount)
                .append(" ProcessedCount=").append(processedCount)
                .append(" PendingCount=").append(pendingCount);
        return b.toString();
    }

}
