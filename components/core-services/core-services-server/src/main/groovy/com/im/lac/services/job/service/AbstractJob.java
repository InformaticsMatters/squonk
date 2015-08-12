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
    
    public JobStatus start(CamelContext context, String username) throws Exception {
        //TODO - convert to abstract method once old job classes have been migrated
        throw new IllegalStateException("Subclasses must override");
    }

}
