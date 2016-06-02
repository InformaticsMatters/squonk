package com.im.lac.job.client;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.core.CommonConstants;
import org.squonk.core.client.config.SquonkClientConfig;
import org.squonk.types.io.JsonHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Client to manage submission and management of jobs. The client only knows about
 * {@link com.im.lac.job.jobdef.JobDefinition}s and {@link com.im.lac.job.jobdef.JobStatus}es, not about how jobs are executed.
 *
 * @author timbo
 */
public class JobClient {

    private static final Logger LOG = Logger.getLogger(JobClient.class.getName());


    private final String base;
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final JsonHandler jsonHandler = new JsonHandler();

    public JobClient(String baseUrl) {
        this.base = baseUrl;
        LOG.info("JobClient using base URL of " + baseUrl);
    }

    public JobClient() {
        this(SquonkClientConfig.INSTANCE.getCoreServicesBaseUrl() + "/jobs");
    }

    /**
     * Get a list of the status of all jobs that match the filters. All jobs are archived so its
     * possible to get details of completed jobs. Jobs are retrieved in inverse submission order
     * (e.g. most recent first).
     *
     * NOTE: all parameters are currently ignored.
     *
     * @param username Username of the authenticated user
     * @param max The maximum number of jobs to retrieve. If less than 1 all jobs matching the other
     * filters are retrieved.
     * @param status One or more status filters. Can be null if no filter.
     * @param submissionTimeStart The submit time for the job submission period. Can be null.
     * @param submissionTimeEnd The end time for the job submission period. Can be null.
     * @param completionTimeStart The submit time for the job completion period. Can be null.
     * @param completionTimeEnd The end time for the job completion period. Can be null.
     * @return A list of job statuses matching the filters
     * @throws java.io.IOException
     */
    public List<JobStatus> getJobStatuses(
            String username,
            int max,
            List<JobStatus> status,
            Date submissionTimeStart,
            Date submissionTimeEnd,
            Date completionTimeStart,
            Date completionTimeEnd) throws IOException {
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        HttpGet httpGet = new HttpGet(base);
        httpGet.setHeader(CommonConstants.HEADER_SQUONK_USERNAME, username);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity1 = response.getEntity();
            if (response.getStatusLine().getStatusCode() != 200) {
                String err = EntityUtils.toString(entity1);
                LOG.log(Level.WARNING, "Request failed: {0}", err);
                throw new IOException("Request failed: " + response.getStatusLine().toString());
            }
            InputStream is = entity1.getContent();
            return jsonHandler.streamFromJson(is, JobStatus.class, true).collect(Collectors.toList());
        }
    }

    /**
     * Submit this as a new Job
     *
     * @param username Username of the authenticated user
     * @param jobdef The definition of the job
     * @return The status of the submitted job, which includes the job ID that can be used to
     * further monitor and handle the job.
     * @throws java.io.IOException
     */
    public JobStatus submitJob(String username, JobDefinition jobdef) throws IOException {
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        String json = jsonHandler.objectToJson(jobdef);
        HttpPost httpPost = new HttpPost(base);
        httpPost.setHeader(CommonConstants.HEADER_SQUONK_USERNAME, username);
        httpPost.setEntity(new StringEntity(json));
        LOG.log(Level.INFO, "submitting job {0} to {1}", new Object[]{jobdef, base});
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            LOG.info(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            String status = EntityUtils.toString(entity);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOG.log(Level.WARNING, "Request failed: {0}", status);
                throw new IOException("Request failed: " + response.getStatusLine().toString());
            }
            LOG.log(Level.INFO, "JobStatus JSON = {0}", status);
            return jsonHandler.objectFromJson(status, JobStatus.class);
        }
    }

    /**
     * Get the current status of the job.
     *
     * @param username Username of the authenticated user
     * @param jobId
     * @return
     * @throws java.io.IOException
     */
    public JobStatus getJobStatus(String username, String jobId) throws IOException {
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        HttpGet httpGet = new HttpGet(base + "/" + jobId);
        httpGet.setHeader(CommonConstants.HEADER_SQUONK_USERNAME, username);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOG.log(Level.WARNING, "Request failed: {0}", json);
                throw new IOException("Request failed: " + response.getStatusLine().toString());
            }
            LOG.log(Level.INFO, "Job JSON = {0}", json);
            return jsonHandler.objectFromJson(json, JobStatus.class);
        }
    }

    /**
     * Cancel the job if it is still running. Note: implementation details are unclear, but it needs
     * to be possible to cancel long running jobs.
     *
     * @param username Username of the authenticated user
     * @param jobId
     * @return
     * @throws java.lang.Exception
     */
    public JobStatus cancel(String username, String jobId) throws Exception {
        throw new UnsupportedOperationException("NYI");
    }

    /**
     * Process any pending results for the job and updateStatus the results. This method can be called
     * many times during a long process and the results updated. The final results are only ready
     * once the return status is {@link JobStatus.Status.COMPLETE}. Note: this only works for
     * certain types of Job. For others it is equivalent to {@link getStatus()}
     *
     * @param username Username of the authenticated user
     * @param jobId
     * @return
     */
    public JobStatus processResults(String username, String jobId) {
        throw new UnsupportedOperationException("NYI");
    }

}
