package com.im.lac.services.job;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.services.job.service.Environment;

/**
 *
 * @author timbo
 * @param <T>
 */
public interface Job<T extends JobDefinition> {
    
    String getJobId();
    
   JobStatus execute(Environment env);
   
   JobStatus getJobStatus(Environment env);
   
   //T getJobDefinition();
    
}
