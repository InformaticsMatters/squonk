package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.im.lac.dataset.DataItem;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author timbo
 * @param <T>
 */
public class JobStatus<T extends JobDefinition> implements Serializable {

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
    private final T jobDefinition;
    private final DataItem result;

    public static <T extends JobDefinition> JobStatus<T> create(T jobDef, Date started) {
        String jobId = UUID.randomUUID().toString();
        return new JobStatus(jobId, Status.PENDING, -1, -1, -1, started, null, jobDef, null);
    }

    public JobStatus(
            @JsonProperty("jobId") String jobId,
            @JsonProperty("status") Status status,
            @JsonProperty("totalCount") int totalCount,
            @JsonProperty("processedCount") int processedCount,
            @JsonProperty("pendingCount") int pendingCount,
            @JsonProperty("started") Date started,
            @JsonProperty("completed") Date completed,
            @JsonProperty("jobDefinition") T jobDefinition,
            @JsonProperty("result") DataItem result) {
        this.jobId = jobId;
        this.status = status;
        this.totalCount = totalCount;
        this.processedCount = processedCount;
        this.pendingCount = pendingCount;
        this.started = started;
        this.completed = completed;
        this.jobDefinition = jobDefinition;
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
        return jobDefinition;
    }

    public DataItem getResult() {
        return result;
    }

    public JobStatus withStatus(Status status, Integer processedCount) {
        Date completed = null;
        // TODO - block certain status transitions e.g. once complete doen't let status be changed
        int processed = (processedCount == null ? -1 : processedCount.intValue());
        if (status == Status.COMPLETED || status == Status.ERROR || status == Status.CANCELLED) {
            // does the date come from java or the database?
            completed = new Date();
        }
        return new JobStatus(this.jobId, status, this.totalCount, processed, 0, this.started, completed, this.jobDefinition, this.result );
    }


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("JobStatus: ").append(status)
                .append(" JobId=").append(jobId)
                .append(" TotalCount=").append(totalCount)
                .append(" ProcessedCount=").append(processedCount)
                .append(" PendingCount=").append(pendingCount)
                .append(" Job Definition=").append(jobDefinition);
        return b.toString();
    }

}
