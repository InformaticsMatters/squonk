/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorUtils;
import org.squonk.execution.ExecutionParameters;
import org.squonk.execution.JobManager;
import org.squonk.io.SquonkDataSource;
import org.squonk.jobdef.JobStatus;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import org.squonk.util.ServiceConstants;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
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

    protected int timerInterval = 5 * 60 * 1000;
    protected int timerDelay =  15 * 1000;
    protected boolean pollForServiceDescriptors = Boolean.valueOf(
            IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_POLL_FOR_SERVICE_DESCRIPTORS", "false"));

    /**
     * Set this environment variable to 'true' to allow testing without authentication, in which
     * case the user 'nobody' is assumed.
     */
    protected static boolean ALLOW_UNAUTHENTICATED_USER = Boolean.valueOf(
            IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_ALLOW_UNAUTHENTICATED", "false"));

    @Inject
    private JobManager jobManager;

    @Override
    public void configure() throws Exception {

        if (ALLOW_UNAUTHENTICATED_USER) {
            LOG.warning("External Job execution is running without authentication");
        }


        if (jobManager != null) {
            jobManager.setCamelContext(getContext());
        }

        if (pollForServiceDescriptors && jobManager != null) {
            from("timer:discoverServiceDescriptors?period=" + timerInterval + "&delay=" + timerDelay)
                    .log(LoggingLevel.WARN, "Polling for service descriptors")
                    .to("http4://coreservices:8080/coreservices/rest/v1/services/descriptors")
                    .process((exch) -> {
                        String resp = exch.getIn().getBody(String.class);
                        List<ServiceDescriptor> sds = ServiceDescriptorUtils.readJsonList(resp);
                        LOG.fine("Discovered " + sds.size() + " service descriptors");
                        jobManager.putServiceDescriptors(sds);
                    });
        }

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
                // submit new async job
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
        Message message = exch.getIn();
        try {
            String username = fetchUsername(message);
            List<JobStatus> jobStatuses = jobManager.getJobs(username);
            InputStream json = JsonHandler.getInstance().marshalStreamToJsonArray(jobStatuses.stream(), false);
            exch.getIn().setBody(json);
        } catch (AuthenticationException e) {
            handle401Error(message);
        }  catch (Exception e) {
            handle500Error(message, "Failed to list jobs", e);
        }
    }

    private void handleJobStatus(Exchange exch) {
        Message message = exch.getIn();
        try {
            JobStatus jobStatus = doHandleJobStatus(exch.getIn());
            if (jobStatus == null) {
                message.setBody("{ \"error\": \"Job not found\"}");
                message.setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
            } else {
                String json = JsonHandler.getInstance().objectToJson(jobStatus);
                message.setBody(json);
            }
        } catch (AuthenticationException e) {
            handle401Error(message);
        } catch (Exception e) {
            handle500Error(message, "Failed to fetch job", e);
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
        LOG.info("Submitting async job");
        Message message = exch.getIn();
        try {
            String username = fetchUsername(message);
            JobStatus jobStatus = doHandleJobSubmit(exch.getIn());
            LOG.info("Job " + jobStatus.getJobId() + " had been submitted");
            String json = JsonHandler.getInstance().objectToJson(jobStatus);
            message.setBody(json);
        } catch (AuthenticationException e) {
            handle401Error(message);
        } catch (Exception e) {
            handle500Error(message, "Failed to submit job", e);
        }
    }

    private JobStatus doHandleJobSubmit(Message message) throws Exception {

        String body = message.getBody(String.class);
        ExecutionParameters params = JsonHandler.getInstance().objectFromJson(body, ExecutionParameters.class);
        Map<String, InputStream> inputs = new HashMap<>();
        String username = fetchUsername(message);
        for (Map.Entry<String, DataHandler> e : message.getAttachments().entrySet()) {
            String name = e.getKey();
            InputStream input = e.getValue().getInputStream();
            inputs.put(name, input);
            LOG.fine("Added input " + name);
        }
        return jobManager.executeAsync(username, params, inputs);
    }


    private void handleJobResults(Exchange exch) {
        Message message = exch.getIn();
        try {
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
                handle500Error(message, "Job not finished", null);
                return;
            }
            String json = JsonHandler.getInstance().objectToJson(jobStatus);
            message.setBody(json);
            message.setHeader(Exchange.CONTENT_TYPE, CommonMimeTypes.MIME_TYPE_TEXT_PLAIN);
            doHandleResults(message, username, id);
        } catch (AuthenticationException e) {
            handle401Error(message);
        } catch (Exception e) {
            handle500Error(message, "Failed to fetch data", e);
        }
    }

    private void doHandleResults(Message message, String username, String jobId) throws Exception {

        // set the results as attachments to the message
        Map<String,List<SquonkDataSource>> outputs = jobManager.getJobResultsAsDataSources(username, jobId);
        LOG.fine("Found " + outputs.size() + " outputs");

        for (Map.Entry<String,List<SquonkDataSource>> e : outputs.entrySet()) {
            for (SquonkDataSource dataSource : e.getValue()) {
                String name = (outputs.size() == 1 ? dataSource.getName() : e.getKey() + "_" + dataSource.getName());
                LOG.fine("Adding attachment " + name + " of type " + dataSource.getContentType());
                //dataSource.materialize();
                message.addAttachment(name, new DataHandler(dataSource));
            }
        }

        // handle gzipping the response if requested to do so
        String accept = message.getHeader("Accept-Encoding", String.class);
        if (accept != null) {
            if (accept.equalsIgnoreCase("gzip")) {
                LOG.fine("Gzipping response");
                message.setHeader("Content-Encoding", "gzip");
            } else {
                LOG.warning("Unsupported Accept-Encoding: " + accept);
            }
        }
    }

    private void handleJobCleanup(Exchange exch) {
        Message message = exch.getIn();
        try {
            String id = message.getHeader("id", String.class);
            String username = fetchUsername(message);
            LOG.info("Handing data request for job " + id);
            JobStatus jobStatus = jobManager.getJobStatus(username, id);
            if (jobStatus == null) {
                message.setBody("{ \"error\": \"Job not found\"}");
                message.setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                return;
            }

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

        } catch (AuthenticationException e) {
            handle401Error(message);
        } catch (Exception e) {
            handle500Error(message, "Failed to cleanup job", e);
        }
    }

    private void handleJobCancel(Exchange exch) {
        Message message = exch.getIn();
        try {
            String id = message.getHeader("id", String.class);
            String username = fetchUsername(message);
            LOG.info("Handing data request for job " + id);
            JobStatus jobStatus = jobManager.getJobStatus(username, id);
            if (jobStatus == null) {
                handle404Error(message, "Job not found");
                return;
            }
            jobStatus = jobManager.cancelJob(username, id);
            LOG.info("Job " + id + " has been removed");
            if (jobStatus != null) {
                String json = JsonHandler.getInstance().objectToJson(jobStatus);
                message.setBody(json);
            } else {
                LOG.log(Level.SEVERE, "No job status returned after cancel");
                message.setBody("{ \"warning\": \"Could not determined JobStatus after cancel. Cleanup may not be complete.\"}");
                message.setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            }
        } catch (AuthenticationException e) {
            handle401Error(message);
        } catch (Exception e) {
            handle500Error(message, "Failed to cancel job", e);
        }

    }

    private String fetchUsername(Message message) throws AuthenticationException {
        String user = null;
        HttpServletRequest request = message.getBody(HttpServletRequest.class);
        if (request != null) {
            Principal p = request.getUserPrincipal();
            if (p != null) {
                user = p.getName();
                if (user.startsWith("service-account-")) {
                    String delegate = message.getHeader(ServiceConstants.HEADER_SQUONK_USERNAME, String.class);
                    if (delegate != null && !delegate.isEmpty()) {
                        LOG.fine("Service account " + user + " specified to run as user " + delegate);
                        user = delegate;
                    }
                }
            }
        }
        if (user == null) {
            if (ALLOW_UNAUTHENTICATED_USER) {
                String delegate = message.getHeader(ServiceConstants.HEADER_SQUONK_USERNAME, String.class);
                if (delegate != null && !delegate.isEmpty()) {
                    user = delegate;
                } else {
                    user = "nobody";
                }
            } else {
                throw new AuthenticationException();
            }
        }
        LOG.fine("User: " + user);
        return user;
    }

    private void handle404Error(Message message, String warning) {
        LOG.severe("Not Found: " + warning);
        message.setBody("{\"error\": \"" + warning + "\"}");
        message.setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
    }

    private void handle401Error(Message message) {
        LOG.severe("Unauthorised");
        message.setBody("{\"error\": \"Unauthorised\"}");
        message.setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
    }

    private void handle500Error(Message message, String warning, Exception e) {
        if (e == null) {
            LOG.severe(warning);
            message.setBody("{\"error\": \"" + warning + "\"}");
        } else {
            LOG.log(Level.SEVERE, warning, e);
            message.setBody("{\"error\": \"" + warning + ": " + e.getLocalizedMessage() + "\"}");
        }
        message.setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
    }

    class AuthenticationException extends Exception {

        AuthenticationException() {
            super("Authentication required");
        }
    }

}
