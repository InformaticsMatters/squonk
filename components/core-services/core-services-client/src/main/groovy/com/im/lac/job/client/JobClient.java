package com.im.lac.job.client;

import com.im.lac.dataset.DataItem;
import com.im.lac.job.jobdef.AsyncLocalProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.io.JsonHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Client to manage submission and management of jobs. The client only knows about
 * {@link JobDefintion}s and {@link JobStatus}es, not about how jobs are executed.
 *
 * WARNING: this class is currently just a skeleton - nothing is implemented.
 *
 * @author timbo
 */
public class JobClient {

    private static final Logger LOG = Logger.getLogger(JobClient.class.getName());

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
     * Get a list of the status of all jobs that match the filters. All jobs are archived so its
     * possible to get details of completed jobs. Jobs are retrieved in inverse submission order
     * (e.g. most recent first).
     *
     * NOTE: all parameters are currently ignored.
     *
     * @param max The maximum number of jobs to retrieve. If less than 1 all jobs matching the other
     * filters are retrieved.
     * @param status One or more status filters. Can be null if no filter.
     * @param submissionTimeStart The start time for the job submission period. Can be null.
     * @param submissionTimeEnd The end time for the job submission period. Can be null.
     * @param completionTimeStart The start time for the job completion period. Can be null.
     * @param completionTimeEnd The end time for the job completion period. Can be null.
     * @return A list of job statuses matching the filters
     * @throws java.io.IOException
     */
    public List<JobStatus> getJobStatuses(
            int max,
            List<JobStatus> status,
            Date submissionTimeStart,
            Date submissionTimeEnd,
            Date completionTimeStart,
            Date completionTimeEnd) throws IOException {

        HttpGet httpGet = new HttpGet(base);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        LOG.fine(response.getStatusLine().toString());
        HttpEntity entity1 = response.getEntity();
        InputStream is = entity1.getContent();
        return jsonHandler.streamFromJson(is, JobStatus.class, true).collect(Collectors.toList());
    }

    /**
     * Submit this as a new Job for asynchronously processing a dataset using a local route
     *
     * @param jobdef The defintion of the job
     * @return The status of the submitted job, which includes the job ID that can be used to
     * further monitor and handle the job.
     * @throws java.lang.Exception
     */
    public JobStatus submitJob(AsyncLocalProcessDatasetJobDefinition jobdef) throws Exception {

        String json = jsonHandler.objectToJson(jobdef);
        HttpPost httpPost = new HttpPost(base);
        httpPost.setEntity(new StringEntity(json));

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            return jsonHandler.objectFromJson(is, JobStatus.class);
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
