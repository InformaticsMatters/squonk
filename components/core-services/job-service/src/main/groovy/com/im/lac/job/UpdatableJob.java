package com.im.lac.job;

import com.im.lac.job.service.impl.Environment;

/**
 *
 * @author timbo
 */
public interface UpdatableJob extends Job {
    
    public JobStatus processResults(Environment env);
}
