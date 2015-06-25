package com.im.lac.jobs;

import com.im.lac.model.JobDefinition;
import com.im.lac.service.Environment;

/**
 *
 * @author timbo
 */
public interface Job<T extends JobDefinition> {
    
    public enum DatasetMode {

        UPDATE, CREATE
    }
    
    String getJobId();
    
   JobStatus execute(Environment env);
   
   JobStatus getJobStatus(Environment env);
   
   //T getJobDefinition();
    
}
