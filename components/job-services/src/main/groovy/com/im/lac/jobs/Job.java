package com.im.lac.jobs;

import com.im.lac.service.Environment;

/**
 *
 * @author timbo
 */
public interface Job {
    
    public enum DatasetMode {

        UPDATE, CREATE
    }
    
    String getJobId();
    
   JobStatus execute(Environment env);
   
   JobStatus getStatus(Environment env);
    
}
