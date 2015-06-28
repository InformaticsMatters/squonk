package com.im.lac.job;

import com.im.lac.jobdef.JobDefinition;
import com.im.lac.job.service.impl.Environment;

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
