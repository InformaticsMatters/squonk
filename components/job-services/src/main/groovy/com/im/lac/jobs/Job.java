package com.im.lac.jobs;

import com.im.lac.service.Environment;

/**
 *
 * @author timbo
 */
public interface Job {
    
   public JobStatus execute(Environment env);
   
   public JobStatus getStatus(Environment env);
    
}
