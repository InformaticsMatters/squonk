package org.squonk.core.service.job;

import org.squonk.jobdef.JobStatus;
import org.squonk.jobdef.JobDefinition;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 * @param <T>
 */
public interface Job<T extends JobDefinition> {

    String getJobId();

    JobStatus getCurrentJobStatus() throws Exception;

    JobStatus getUpdatedJobStatus() throws Exception;

    JobStatus.Status getStatus() throws Exception;
    
    void setStatus(JobStatus.Status status) throws Exception;
    
    T getJobDefinition();

    JobStatus start(CamelContext context, String username, Integer totalCount) throws Exception;

}
