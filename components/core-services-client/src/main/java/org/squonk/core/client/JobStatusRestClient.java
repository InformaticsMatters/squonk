/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core.client;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.squonk.client.JobStatusClient;
import org.squonk.core.config.SquonkClientConfig;
import org.squonk.jobdef.*;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.ServiceConstants;

import javax.enterprise.inject.Default;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 06/01/16.
 */
@Default
public class JobStatusRestClient extends AbstractHttpClient implements JobStatusClient {

    private static final Logger LOG = Logger.getLogger(JobStatusRestClient.class.getName());

    private final String baseUrl;

    public JobStatusRestClient(String serverUrl) {
        this.baseUrl = serverUrl + SquonkClientConfig.CORE_SERVICES_PATH + "/jobs";
        LOG.info("JobStatusRestClient is using base URL of " + baseUrl);
    }

    public JobStatusRestClient() {
        this(SquonkClientConfig.CORE_SERVICES_SERVER);
    }

    /**
     * Submit this as a new Job. This is the entrypoint for submitting a job.
     *
     * @param username Username of the authenticated user
     * @param jobDef The definition of the job
     * @return The status of the submitted job, which includes the job ID that can be used to
     * further monitor and handle the job.
     * @throws java.io.IOException
     */
    public JobStatus submit(CellExecutorJobDefinition jobDef, String username, Integer totalCount) throws IOException {
        if (jobDef == null) {
            throw new IllegalStateException("Job definition must be specified");
        }
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        String json = toJson(jobDef);
        URIBuilder b = new URIBuilder().setPath(baseUrl);
        if (totalCount != null && totalCount > 0) {
            b = b.setParameter(ServiceConstants.HEADER_JOB_SIZE, totalCount.toString());
        }
        LOG.info("About to post job of " + json);
        InputStream result = executePostAsInputStream( b, json, new BasicNameValuePair(ServiceConstants.HEADER_SQUONK_USERNAME, username));
        return fromJson(result, JobStatus.class);
    }

    public JobStatus create(ExternalJobDefinition jobDef, String username, Integer totalCount) throws IOException {
        if (jobDef == null) {
            throw new IllegalStateException("Job definition must be specified");
        }
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        String json = toJson(jobDef);
        URIBuilder b = new URIBuilder().setPath(baseUrl);
        if (totalCount != null && totalCount > 0) {
            b = b.setParameter(ServiceConstants.HEADER_JOB_SIZE, totalCount.toString());
        }
        LOG.info("About to post job of " + json);
        InputStream result = executePostAsInputStream( b, json, new BasicNameValuePair(ServiceConstants.HEADER_SQUONK_USERNAME, username));
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
        LOG.fine("Updating status for job " + id + " Status: " + status + " Event: "+ event +
                " Processed: " + processedCount + " Errors: " + errorCount);
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + id);
        if (status != null) {
            b.setParameter("status", status.toString());
        }
        if (processedCount != null) {
            b.setParameter(ServiceConstants.HEADER_JOB_PROCESSED_COUNT, processedCount.toString());
        }
        if (errorCount != null) {
            b.setParameter(ServiceConstants.HEADER_JOB_ERROR_COUNT, errorCount.toString());
        }
        InputStream result = executePostAsInputStream( b, event, new NameValuePair[0]);
        JobStatus jobStatus = fromJson(result, JobStatus.class);
        LOG.fine("JobStatus updated: " + jobStatus);
        return jobStatus;
    }

    public JobStatus incrementCounts(String id, int processedCount, int errorCount) throws IOException {
        return updateStatus(id, null, null, processedCount, errorCount);
    }
}
