package com.im.lac.services.job;

import com.im.lac.services.job.service.Environment;

/**
 *
 * @author timbo
 */
public interface CancellableJob extends Job {
    
    public boolean cancel(Environment env);
}
