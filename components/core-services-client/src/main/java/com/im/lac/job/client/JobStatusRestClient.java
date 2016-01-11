package com.im.lac.job.client;

import com.im.lac.client.AbstractHttpClient;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.CommonConstants;
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
        this.baseUrl = IOUtils.getConfiguration("SQUONK_SERVICES_CORE", "http://localhost/coreservices/rest/v1") + "/jobstatus";
    }

    /** Post this Job.
     *
     * @param jobdef The Job definition
     * @param username
     * @param totalCount The total number of work units, or null if unknown
     * @return The current job status. Hopefully the job is RUNNING, but it could already have caused an ERROR
     */
    public JobStatus create(JobDefinition jobdef, String username, Integer totalCount) throws IOException {
        LOG.fine("create() " + jobdef + " - " + username);
        URIBuilder b = new URIBuilder().setPath(baseUrl);
        String json = toJson(jobdef);
        LOG.fine("About to post job of " + json);
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

    public JobStatus updateStatus(String id, JobStatus.Status status, String event, Integer processedCount) throws IOException {
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + id);
        if (status != null) {
            b.setParameter("status", status.toString());
        }
        if (processedCount != null) {
            b.setParameter("processedCount", processedCount.toString());
        }
        InputStream result = executePostAsInputStream( b, event, new NameValuePair[0]);
        return fromJson(result, JobStatus.class);
    }

    public JobStatus incrementProcesssedCount(String id, int count) throws IOException {
        return updateStatus(id, null, null, count);
    }
}
