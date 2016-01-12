package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.im.lac.dataset.DataItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private final String username;
    private final Status status;
    private final int totalCount;
    private final int processedCount;;
    private final Date started;
    private final Date completed;
    private final T jobDefinition;
    private final DataItem result;
    private final List<String> events = new ArrayList<>();

    public static <T extends JobDefinition> JobStatus<T> create(T jobDef, String username, Date started, Integer totalCount) {
        String jobId = UUID.randomUUID().toString();
        return new JobStatus(jobId, username, Status.PENDING, totalCount == null ? 0 : totalCount, 0, started, null, jobDef, null, null);
    }

    public JobStatus(
            @JsonProperty("jobId") String jobId,
            @JsonProperty("username") String username,
            @JsonProperty("status") Status status,
            @JsonProperty("totalCount") int totalCount,
            @JsonProperty("processedCount") int processedCount,
            @JsonProperty("started") Date started,
            @JsonProperty("completed") Date completed,
            @JsonProperty("jobDefinition") T jobDefinition,
            @JsonProperty("result") DataItem result,
            @JsonProperty("events") List<String> events
    ) {
        this.jobId = jobId;
        this.username = username;
        this.status = status;
        this.totalCount = totalCount;
        this.processedCount = processedCount;
        this.started = started;
        this.completed = completed;
        this.jobDefinition = jobDefinition;
        this.result = result;
        if (events != null) {
            this.events.addAll(events);
        }
    }

    public String getJobId() {
        return jobId;
    }

    public String getUsername() {
        return username;
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

    public List<String> getEvents() {
        return events;
    }

    public JobStatus withEvent(String event) {
        List neu = new ArrayList<>();
        neu.addAll(events);
        neu.add(event);
        return new JobStatus(this.jobId, this.username, status, this.totalCount, this.processedCount, this.started, completed, this.jobDefinition, this.result, neu);
    }

    public JobStatus withStatus(Status status, Integer processedCount, String event) {
        Date completed = null;
        // TODO - block certain status transitions e.g. once complete doen't let status be changed
        int processed = (processedCount == null ? -1 : processedCount.intValue());
        if (status == Status.COMPLETED || status == Status.ERROR || status == Status.CANCELLED) {
            // does the date come from java or the database?
            completed = new Date();
        }
        if (event != null) {
            List neu = new ArrayList<>();
            neu.addAll(events);
            neu.add(event);
            return new JobStatus(this.jobId, this.username, status, this.totalCount, processed, this.started, completed, this.jobDefinition, this.result, neu);
        } else {
            return new JobStatus(this.jobId, this.username, status, this.totalCount, processed, this.started, completed, this.jobDefinition, this.result, this.events);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("JobStatus: ").append(status)
                .append(" JobId=").append(jobId)
                .append(" Username=").append(username)
                .append(" Status=").append(status.toString())
                .append(" TotalCount=").append(totalCount)
                .append(" ProcessedCount=").append(processedCount);
        return b.toString();
    }

}
