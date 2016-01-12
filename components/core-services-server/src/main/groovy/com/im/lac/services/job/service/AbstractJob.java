package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.job.Job;
import java.util.UUID;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 * @param <T>
 */
public abstract class AbstractJob<T extends JobDefinition> implements Job<T> {

    protected final String jobId;
    protected JobStatus.Status status;

    protected AbstractJob() {
        jobId = UUID.randomUUID().toString();
    }

    protected AbstractJob(String jobId) {
        this.jobId = jobId;
    }

    protected AbstractJob(String jobId, JobStatus.Status status) {
        this.jobId = jobId;
        this.status = status;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    @Override
    public JobStatus.Status getStatus() {
        return status;
    }
    
    @Override
    public void setStatus(JobStatus.Status status) {
        this.status = status;
    }

    @Override
    public JobStatus start(CamelContext context, String username) throws Exception {
        //TODO - convert to abstract method once old job classes have been migrated
        throw new IllegalStateException("Subclasses must override");
    }

}
