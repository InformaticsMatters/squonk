package com.im.lac.jobs;

import com.im.lac.service.Environment;

/**
 *
 * @author timbo
 */
public interface UpdatableJob extends Job {
    
    public JobStatus processResults(Environment env);
}
