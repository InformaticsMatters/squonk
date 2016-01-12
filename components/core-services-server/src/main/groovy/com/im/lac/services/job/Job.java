package com.im.lac.services.job;

import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.JobDefinition;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 * @param <T>
 */
public interface Job<T extends JobDefinition> {

    String getJobId();

    JobStatus getCurrentJobStatus();

    JobStatus getUpdatedJobStatus();

    JobStatus.Status getStatus();
    
    void setStatus(JobStatus.Status status);
    
    T getJobDefinition();

    JobStatus start(CamelContext context, String username) throws Exception;

}
