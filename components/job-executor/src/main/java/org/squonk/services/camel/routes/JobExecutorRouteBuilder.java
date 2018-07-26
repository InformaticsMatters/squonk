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

package org.squonk.services.camel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.ExecutionParameters;
import org.squonk.execution.JobManager;
import org.squonk.io.SquonkDataSource;
import org.squonk.jobdef.JobStatus;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;

import javax.activation.DataHandler;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * External execution of squonk services as a job through a REST API
 */
public class JobExecutorRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(JobExecutorRouteBuilder.class.getName());
    private static final String ROUTE_STATS = "seda:post_stats";

    @Inject
    private JobManager jobManager;

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");


        /* These are the REST endpoints - exposed as public web services
         */

        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();


        rest("/v1/jobs").description("Job execution and management")
                //
                // list the jobs
                .get("/").description("List the jobs for the current user")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .outType(List.class)
                .route()
                .process((Exchange exch) -> {
                    handleJobsList(exch);
                })
                .endRest()
                //
                // get the job status
                .get("/{id}/status").description("Get the latest status of an individual job")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    handleJobStatus(exch);
                })
                .endRest()
                //
                // submit new job
                .post("/").description("Submit a new job")
                .bindingMode(RestBindingMode.off)
                .outType(JobStatus.class)
                .route()
                .log("handling Job posting")
                .unmarshal().mimeMultipart()
                .process((Exchange exch) -> {
                    handleJobSubmit(exch);
                })
                .endRest()
                //
                // get the job results
                .get("/{id}/results").description("Get the results for an individual job")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_MULTIPART_MIXED)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    handleJobResults(exch);
                })
                .marshal().mimeMultipart()
                .endRest()
                //
                // halt the job
                .get("/{id}/terminate").description("Terminate an individual job")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    handleJobCancel(exch);
                })
                .endRest()
                //
                // cleanup the job
                .delete("/{id}").description("Cleanup an individual job")
                .bindingMode(RestBindingMode.off)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    handleJobCleanup(exch);
                })
                .marshal().mimeMultipart()
                .endRest()
        ;
    }

    private void handleJobsList(Exchange exch) {
        try {
            Message message = exch.getIn();
            String username = fetchUsername(message);
            List<JobStatus> jobStatuses = jobManager.getJobs(username);
            InputStream json = JsonHandler.getInstance().marshalStreamToJsonArray(jobStatuses.stream(), false);
            exch.getIn().setBody(json);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to list jobs", e);
            exch.getIn().setBody("{ \"error\": \"Failed to list jobs: " + e.getLocalizedMessage() + "\"}");
            exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        }
    }

    private void handleJobStatus(Exchange exch) {
        try {
            JobStatus jobStatus = doHandleJobStatus(exch.getIn());
            if (jobStatus == null) {
                exch.getIn().setBody("{ \"error\": \"Job not found\"}");
                exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
            } else {
                String json = JsonHandler.getInstance().objectToJson(jobStatus);
                exch.getIn().setBody(json);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to fetch job", e);
            exch.getIn().setBody("{ \"error\": \"Failed to fetch job: " + e.getLocalizedMessage() + "\"}");
            exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        }
    }

    private JobStatus doHandleJobStatus(Message message) throws Exception {
        String id = message.getHeader("id", String.class);
        String username = fetchUsername(message);
        LOG.info("Fetching Job " + id);
        JobStatus jobStatus = jobManager.getJobStatus(username, id);
        LOG.info("Fetched Job " + jobStatus);
        return jobStatus;
    }

    private void handleJobSubmit(Exchange exch) {
        LOG.info("Submitting job");
        try {
            JobStatus jobStatus = doHandleJobSubmit(exch.getIn());
            LOG.info("Job " + jobStatus.getJobId() + " had been submitted");
            String json = JsonHandler.getInstance().objectToJson(jobStatus);
            exch.getIn().setBody(json);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to submit job", e);
            exch.getIn().setBody("{ \"error\": \"Job failed to submit: " + e.getLocalizedMessage() + "\"}");
            exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        }
    }

    private JobStatus doHandleJobSubmit(Message message) throws Exception {

        String body = message.getBody(String.class);
        ExecutionParameters params = JsonHandler.getInstance().objectFromJson(body, ExecutionParameters.class);
        ServiceDescriptor serviceDescriptor = params.getServiceDescriptor();
        Map<String, Object> options = params.getOptions();
        Map<String, InputStream> inputs = new HashMap<>();
        String username = fetchUsername(message);
        for (Map.Entry<String, DataHandler> e : message.getAttachments().entrySet()) {
            String name = e.getKey();
            InputStream input = e.getValue().getInputStream();
            inputs.put(name, input);
            LOG.fine("Added input " + name);
        }

        return jobManager.executeAsync(username, serviceDescriptor, options, inputs);
    }

    private void handleJobResults(Exchange exch) {
        Message message = exch.getIn();
        String id = message.getHeader("id", String.class);
        String username = fetchUsername(message);
        LOG.info("Handing data request for job " + id);
        JobStatus jobStatus = jobManager.getJobStatus(username, id);
        if (jobStatus == null) {
            message.setBody("{ \"error\": \"Job not found\"}");
            message.setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
            return;
        }
        if (jobStatus.getStatus() != JobStatus.Status.RESULTS_READY) {
            message.setBody("{ \"error\": \"Job not finished\"}");
            message.setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            return;
        }
        try {
            String json = JsonHandler.getInstance().objectToJson(jobStatus);
            message.setBody(json);
            message.setHeader(Exchange.CONTENT_TYPE, CommonMimeTypes.MIME_TYPE_TEXT_PLAIN);
            List<SquonkDataSource> dataSources = jobManager.getJobResultsAsDataSources(username, id);
            LOG.finer("Found " + dataSources.size() + " DataHandlers");
            for (SquonkDataSource dataSource : dataSources) {
                LOG.info("Adding attachment " + dataSource.getName() + " of type " + dataSource.getContentType());
                message.addAttachment(dataSource.getName(), new DataHandler(dataSource));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to fetch data", e);
            exch.getIn().setBody("{ \"error\": \"Failed to fetch data: " + e.getLocalizedMessage() + "\"}");
            exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        }
    }

    private void handleJobCleanup(Exchange exch) {
        Message message = exch.getIn();
        String id = message.getHeader("id", String.class);
        String username = fetchUsername(message);
        LOG.info("Handing data request for job " + id);
        JobStatus jobStatus = jobManager.getJobStatus(username, id);
        if (jobStatus == null) {
            message.setBody("{ \"error\": \"Job not found\"}");
            message.setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
            return;
        }
        try {
            jobStatus = jobManager.cleanupJob(username, id);
            LOG.info("Job " + id + " has been removed");
            if (jobStatus != null) {
                String json = JsonHandler.getInstance().objectToJson(jobStatus);
                exch.getIn().setBody(json);
            } else {
                LOG.log(Level.SEVERE, "No job status returned after cleanup");
                exch.getIn().setBody("{ \"warning\": \"Could not determined JobStatus after cleanup. Cleanup may not be complete.\"}");
                exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to cleanup job", e);
            exch.getIn().setBody("{ \"error\": \"Failed to cleanup: " + e.getLocalizedMessage() + "\"}");
            exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        }
    }

    private void handleJobCancel(Exchange exch) {
        Message message = exch.getIn();
        String id = message.getHeader("id", String.class);
        String username = fetchUsername(message);
        LOG.info("Handing data request for job " + id);
        JobStatus jobStatus = jobManager.getJobStatus(username, id);
        if (jobStatus == null) {
            message.setBody("{ \"error\": \"Job not found\"}");
            message.setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
            return;
        }
        try {
            jobStatus = jobManager.cancelJob(username, id);
            LOG.info("Job " + id + " has been removed");
            if (jobStatus != null) {
                String json = JsonHandler.getInstance().objectToJson(jobStatus);
                exch.getIn().setBody(json);
            } else {
                LOG.log(Level.SEVERE, "No job status returned after cancel");
                exch.getIn().setBody("{ \"warning\": \"Could not determined JobStatus after cancel. Cleanup may not be complete.\"}");
                exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to cancel job", e);
            exch.getIn().setBody("{ \"error\": \"Failed to cancel job: " + e.getLocalizedMessage() + "\"}");
            exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        }

    }

    private String fetchUsername(Message message) {
        return message.getHeader("SquonkUsername", String.class);
    }

}
