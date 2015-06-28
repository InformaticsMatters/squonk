package com.im.lac.job.service.impl;

import com.im.lac.job.Job;

/**
 *
 * @author timbo
 */
public interface JobStore {
    
    Job getJob(String jobid);
    
    void putJob(Job job);
    
    boolean removeJob(String jobId);
    
}
