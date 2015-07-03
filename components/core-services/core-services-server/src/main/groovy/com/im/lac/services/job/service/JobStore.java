package com.im.lac.services.job.service;

import com.im.lac.services.job.Job;
import java.util.List;

/**
 *
 * @author timbo
 */
public interface JobStore {
    
    Job getJob(String jobid);
    
    void putJob(Job job);
    
    boolean removeJob(String jobId);
    
    List<Job> getJobs();
    
}
