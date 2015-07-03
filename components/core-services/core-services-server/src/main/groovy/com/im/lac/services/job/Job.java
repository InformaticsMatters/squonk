package com.im.lac.services.job;

import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.JobDefinition;

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

}
