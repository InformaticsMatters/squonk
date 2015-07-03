package com.im.lac.services.job;

import com.im.lac.job.jobdef.JobStatus;

/**
 *
 * @author timbo
 */
public interface UpdatableJob extends Job {
    
    public JobStatus processResults();
}
