package com.im.lac.service;

import com.im.lac.jobs.Job;
import com.im.lac.jobs.JobStatus;
import com.im.lac.jobs.impl.AsynchronousJob;
import com.im.lac.model.ProcessDatasetJobDefinition;
import java.util.Date;
import java.util.List;

/**
 * Service to manage submission and management of jobs. Executing jobs will generate metrics about
 * those jobs. Those metrics might be available by an extension to this service, or a separate
 * service.
 *
 * TODO - work out how Jobs are created based on some job definition
 *
 * @author timbo
 */
public class JobService {

    private final Environment env;

    JobService(Environment env) {
        this.env = env;
    }

    /**
     * Get a list of all jobs that match the filters. All jobs are archived so its possible to get
     * details of completed jobs. Jobs are retrieved in inverse submission order (e.g. most recent
     * first).
     *
     * @param max The maximum number of jobs to retrieve. If less than 1 all jobs matching the other
     * filters are retrieved.
     * @param status One or more status filters. Can be null if no filter.
     * @param submissionTimeStart The start time for the job submission period. Can be null.
     * @param submissionTimeEnd The end time for the job submission period. Can be null.
     * @param completionTimeStart The start time for the job completion period. Can be null.
     * @param completionTimeEnd The end time for the job completion period. Can be null.
     * @return A list of jobs matching the filters
     */
    public List<Job> getJobs(
            int max,
            List<JobStatus> status,
            Date submissionTimeStart,
            Date submissionTimeEnd,
            Date completionTimeStart,
            Date completionTimeEnd) {
        throw new UnsupportedOperationException("NYI");
    }

    /**
     * Submit this as a new Job
     *
     * @param jobdef
     * @return The status of the submitted job, which includes the job ID that can be used to
     * further monitor and handle the job.
     */
    public JobStatus submitProcessDatasetJob(ProcessDatasetJobDefinition jobdef) {
        // TODO - handle the job stats in some way
        // TODO - persist the job in some way
        // TODO - do we need a version of this with a callback that notifies you when complete?
        Job job = createProcessDatasetJobJob(jobdef);
        return job.execute(env);
    }

    private Job createProcessDatasetJobJob(ProcessDatasetJobDefinition jobdef) {
        switch (jobdef.getExecutionMode()) {
            case ASYNC_SIMPLE:
                return new AsynchronousJob(jobdef);
            case ASYNC_STREAMING:
                return null;
            case ASYNC_SPLIT_AND_QUEUE:
                return null;

            default:
                throw new IllegalStateException("Unsupported mode: " + jobdef.getExecutionMode());
        }
    }

    /**
     * Get the current status of the job.
     *
     * @param jobId
     * @return
     */
    public JobStatus getStatus(String jobId) {
        throw new UnsupportedOperationException("NYI");
    }

    /**
     * Cancel the job if it is still running. Note: implementation details are unclear, but it needs
     * to be possible to cancel long running jobs.
     *
     * @param jobId
     * @return
     */
    public JobStatus cancel(String jobId) {
        throw new UnsupportedOperationException("NYI");
    }

    /**
     * Process any pending results for the job and update the results. This method can be called
     * many times during a long process and the results updated. The final results are only ready
     * once the return status is {@link JobStatus.Status.COMPLETE}. Note: this only works for
     * certain types of Job. For others it is equivalent to {@link getStatus()}
     *
     * @param jobId
     * @return
     */
    public JobStatus processResults(String jobId) {
        throw new UnsupportedOperationException("NYI");
    }

}
