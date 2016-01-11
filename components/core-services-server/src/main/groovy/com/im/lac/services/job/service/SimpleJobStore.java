package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.job.Job;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class SimpleJobStore implements JobStore {

    Map<String, Job> jobs = new LinkedHashMap<>();

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

    @Override
    public List<Job> getJobs() {
        List list = new ArrayList();
        list.addAll(jobs.values());
        return list;
    }

    @Override
    public void updateJob(Job job) {
        // TODO - check that this is a valid job ID?
        jobs.put(job.getJobId(), job);
    }

    @Override
    public List<JobStatus> listJobs(JobQuery query) {
        // query is ignored
        try {
            List<JobStatus> list = new ArrayList<>();
            for (Job job : getJobs()) {
                list.add(job.getCurrentJobStatus());
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list jobs", e);
        }
    }

}
