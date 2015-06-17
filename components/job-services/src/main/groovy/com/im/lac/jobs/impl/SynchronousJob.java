package com.im.lac.jobs.impl;

import com.im.lac.service.*;
import com.im.lac.jobs.JobStatus;
import com.im.lac.model.DataItem;
import com.im.lac.model.JobDefinition;
import com.im.lac.model.ProcessDatasetJobDefinition;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Job that executes synchronously (and expected to be fast!), processes the results and returns.
 * Expected that all request-response service calls would be wrapped as this type of job so that the
 * appropriate audit and metrics are generated.
 *
 * @author timbo
 */
public class SynchronousJob<T extends JobDefinition> extends AbstractJob<T> {

    private static final Logger LOG = Logger.getLogger(SynchronousJob.class.getName());

    public SynchronousJob(T jobdef) {
        super(jobdef);
    }

    /**
     * Executes the job synchronously and writes the result to the result set.
     *
     */
    @Override
    protected void doExecute(Environment env) throws Exception {      
        LOG.log(Level.FINE, "SynchronousJob.execute() {0}", jobdef);
        DataItem result = JobExecutor.submitAndWait(env, (ProcessDatasetJobDefinition)jobdef);
        this.processedCount = 1;
        this.completed = new Date();
        this.status = JobStatus.Status.COMPLETED;
    }

}
