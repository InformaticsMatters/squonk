/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.DoNothingJobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import java.util.Date;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 * @param <T>
 */
public class DoNothingJob<T extends DoNothingJobDefinition> extends AbstractJob<T> {

    private final JobStatus jobStatus;
    private final T jobdef;

    public DoNothingJob(T jobdef) {
        super();
        this.jobdef = jobdef;
        Date now = new Date();
        jobStatus = new JobStatus(jobId, JobStatus.Status.COMPLETED, 0, 0, 0, now, now, jobdef, null);
    }

    @Override
    public T getJobDefinition() {
        return jobdef;
    }

    @Override
    public JobStatus getCurrentJobStatus() {
        return jobStatus;
    }

    @Override
    public JobStatus getUpdatedJobStatus() {
        return jobStatus;
    }

    @Override
    public JobStatus.Status getStatus() {
        return jobStatus.getStatus();
    }

    @Override
    public JobStatus start(CamelContext context, String username) throws Exception {
        
        JobStore jobStore = JobHandler.getJobStore(context);
        jobStore.putJob(this);
        return getCurrentJobStatus();
    }
    
}
