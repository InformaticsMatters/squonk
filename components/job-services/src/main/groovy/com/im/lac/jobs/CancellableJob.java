package com.im.lac.jobs;

import com.im.lac.service.Environment;

/**
 *
 * @author timbo
 */
public interface CancellableJob extends Job {
    
    public boolean cancel(Environment env);
}
