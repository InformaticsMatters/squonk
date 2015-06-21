package com.im.lac.service.impl;

import com.im.lac.jobs.Job;
import com.im.lac.service.JobStore;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class SimpleJobStore implements JobStore {
    
    Map<String,Job> jobs = new LinkedHashMap<>();

    @Override
    public Job getJob(String jobid) {
        return jobs.get(jobid);
    }

    @Override
    public void putJob(Job job) {
        jobs.put(job.getJobId(), job);
    }

    @Override
    public boolean removeJob(String jobId) {
        return jobs.remove(jobId) != null;
    }
    
}
