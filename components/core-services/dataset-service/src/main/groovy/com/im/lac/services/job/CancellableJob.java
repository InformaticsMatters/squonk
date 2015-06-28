package com.im.lac.job;

import com.im.lac.job.service.impl.Environment;

/**
 *
 * @author timbo
 */
public interface CancellableJob extends Job {
    
    public boolean cancel(Environment env);
}
