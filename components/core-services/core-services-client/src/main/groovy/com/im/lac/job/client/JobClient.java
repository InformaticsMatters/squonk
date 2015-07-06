package com.im.lac.job.client;


import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.io.JsonHandler;
import java.util.Date;
import java.util.List;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Client to manage submission and management of jobs.
 * The client only knows about {@link JobDefintion}s and {@link JobStatus}es, not about how jobs are
 * executed.
 * 
 * WARNING: this class is currently just a skeleton - nothing is implemented.
 *
 * @author timbo
 */
public class JobClient {
    
    private static final String DEFAULT_BASE_URL = "http://demos.informaticsmatters.com:8080/coreservices/rest/v1/jobs";

    private final String base;
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final JsonHandler jsonHandler = new JsonHandler();

    public JobClient(String baseUrl) {
        this.base = baseUrl;
    }

    public JobClient() {
        base = DEFAULT_BASE_URL;
    }


    /**
     * Get a list of the status of all jobs that match the filters. All jobs are archived so its possible to get
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
     * @return A list of job statuses matching the filters
     */
    public List<JobStatus> getJobStatuses(
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
    public JobStatus submitJob(JobDefinition jobdef) {
        throw new UnsupportedOperationException("NYI");
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
