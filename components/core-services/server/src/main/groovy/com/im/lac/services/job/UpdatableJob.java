package com.im.lac.services.job;

import com.im.lac.services.job.service.Environment;

/**
 *
 * @author timbo
 */
public interface UpdatableJob extends Job {
    
    public JobStatus processResults(Environment env);
}
