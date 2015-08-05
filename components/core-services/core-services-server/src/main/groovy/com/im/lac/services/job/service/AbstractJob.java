package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.services.job.Job;
import java.util.UUID;

/**
 *
 * @author timbo
 * @param <T>
 */
public abstract class AbstractJob<T extends JobDefinition> implements Job<T> {

    protected final String jobId;

    protected AbstractJob() {
        jobId = UUID.randomUUID().toString();
    }
    
    protected AbstractJob(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

}
