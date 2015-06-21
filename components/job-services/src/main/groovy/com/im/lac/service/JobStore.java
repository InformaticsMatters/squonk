package com.im.lac.service;

import com.im.lac.jobs.Job;

/**
 *
 * @author timbo
 */
public interface JobStore {
    
    Job getJob(String jobid);
    
    void putJob(Job job);
    
    boolean removeJob(String jobId);
    
}
