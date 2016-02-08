package com.im.lac.job.client;

import com.im.lac.client.AbstractHttpClient;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.core.CommonConstants;
import static org.squonk.core.CommonConstants.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.squonk.client.JobStatusClient;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 06/01/16.
 */
public class JobStatusRestClient extends AbstractHttpClient implements JobStatusClient {

    private static final Logger LOG = Logger.getLogger(JobStatusRestClient.class.getName());

    private final String baseUrl;

    public JobStatusRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JobStatusRestClient() {
        this.baseUrl = IOUtils.getConfiguration("SQUONK_SERVICES_CORE", "http://localhost/coreservices/rest/v1") + "/jobs";
    }

    /**
     * Submit this as a new Job. This is the entrypoint for submitting a job.
     *
     * @param username Username of the authenticated user
     * @param jobdef The definition of the job
     * @return The status of the submitted job, which includes the job ID that can be used to
     * further monitor and handle the job.
     * @throws java.io.IOException
     */
    public JobStatus submit(JobDefinition jobdef, String username, Integer totalCount) throws IOException {
        if (jobdef == null) {
            throw new IllegalStateException("Job definition must be specified");
        }
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        String json = toJson(jobdef);
        URIBuilder b = new URIBuilder().setPath(baseUrl);
        if (totalCount != null && totalCount > 0) {
            b = b.setParameter(CommonConstants.HEADER_JOB_SIZE, totalCount.toString());
        }
        LOG.info("About to post job of " + json);
        InputStream result = executePostAsInputStream( b, json, new BasicNameValuePair(CommonConstants.HEADER_SQUONK_USERNAME, username));
        return fromJson(result, JobStatus.class);
    }

    /** Fetch the current status for the job with this ID
     *
     * @param id The Job ID
     * @return
     */
    public JobStatus get(String id) throws IOException {
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + id);
        String s = executeGetAsString(b);
        return fromJson(s, JobStatus.class);
    }

    /** Fetch jobs matching these query criteria
     *
     * @param query
     * @return
     */
    public List<JobStatus> list(JobQuery query) throws IOException {
        URIBuilder b = new URIBuilder().setPath(baseUrl);
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, JobStatus.class, true).collect(Collectors.toList());
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to fetch statuses", ioe);
        }
    }

    public JobStatus updateStatus(String id, JobStatus.Status status, String event, Integer processedCount, Integer errorCount) throws IOException {
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + id);
        if (status != null) {
            b.setParameter("status", status.toString());
        }
        if (processedCount != null) {
            b.setParameter(HEADER_JOB_PROCESSED_COUNT, processedCount.toString());
        }
        if (errorCount != null) {
            b.setParameter(HEADER_JOB_ERROR_COUNT, errorCount.toString());
        }
        InputStream result = executePostAsInputStream( b, event, new NameValuePair[0]);
        return fromJson(result, JobStatus.class);
    }

    public JobStatus incrementCounts(String id, int processedCount, int errorCount) throws IOException {
        return updateStatus(id, null, null, processedCount, errorCount);
    }
}
