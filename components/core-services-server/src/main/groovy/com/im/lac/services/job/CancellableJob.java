package com.im.lac.services.job;


/**
 *
 * @author timbo
 */
public interface CancellableJob extends Job {
    
    public boolean cancel();
}
