/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.camel.Attachment;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultAttachment;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.core.ServiceConfig;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorToOpenAPIConverter;
import org.squonk.core.ServiceDescriptorUtils;
import org.squonk.execution.JobManager;
import org.squonk.io.SquonkDataSource;
import org.squonk.jobdef.JobStatus;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import org.squonk.util.ServiceConstants;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * External execution of squonk services as a job through a REST API
 */
public class JobExecutorRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(JobExecutorRouteBuilder.class.getName());
    private static final String ROUTE_STATS = "seda:post_stats";
    private static final String CONTENT_TRANSFER_ENCODING = IOUtils.getConfiguration("CONTENT_TRANSFER_ENCODING", "8bit");

    protected int timerInterval = 5 * 60 * 1000;
    protected int timerDelay = 15 * 1000;
    protected boolean pollForServiceDescriptors = Boolean.valueOf(
            IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_POLL_FOR_SERVICE_DESCRIPTORS", "false"));

    /**
     * Set this environment variable to 'true' to allow testing without authentication, in which
     * case the user 'nobody' is assumed.
     */
    protected static boolean ALLOW_UNAUTHENTICATED_USER = Boolean.valueOf(
            IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_ALLOW_UNAUTHENTICATED", "false"));

    protected static final String JOBS_SERVER = IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_SERVER", null);
    protected static final String JOBS_PATH = IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_PATH", "/jobexecutor/rest");

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
                    .log(LoggingLevel.INFO, "Polling for service descriptors")
                    .to("http4://coreservices:8080/coreservices/rest/v1/services/descriptors")
                    .process((exch) -> {
                        String resp = exch.getIn().getBody(String.class);
                        List<ServiceDescriptor> sds = ServiceDescriptorUtils.readJsonList(resp);
                        LOG.info("Discovered " + sds.size() + " service descriptors");
                        jobManager.putServiceDescriptors(sds);
                    });
        } else {
            LOG.warning("Service descriptors are not being dynamically updated");
        }

        restConfiguration()
                .component("servlet")
                .host("0.0.0.0")
                .enableCORS(true)
                .corsHeaderProperty("Access-Control-Allow-Headers",
                        "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, " +
                                "Access-Control-Request-Headers, Authorization")
                .corsHeaderProperty("Access-Control-Allow-Credentials", "true")
        ;


        /* These are the REST endpoints - exposed as public web services
         */

        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();

        // everything under /rest/v1 will potentially be secured except for /rest/v1/swagger
        rest("/v1/swagger/swagger.json").description("Service information")
                //
                //
                .get("/").description("Get OpenAPI definitions for services as JSON")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .route()
                .process((Exchange exch) -> {
                    handleFetchSwagger(exch, "json");
                })
                .endRest();

        rest("/v1/swagger/swagger.yaml").description("Service information")
                //
                //
                .get("/").description("Get OpenAPI definitions for services as YAML")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_YAML)
                .route()
                .process((Exchange exch) -> {
                    handleFetchSwagger(exch, "yaml");
                })
                .endRest();

        rest("/v1/services").description("Service information")
                //
                //
                .get("/").description("Get a summary of the available service descriptors")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .route()
                .process((Exchange exch) -> {
                    handleFetchServiceDescriptorInfo(exch);
                })
                .endRest()
                //
                //
                .get("/{id}").description("Get service config for a specific service")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .outType(ServiceDescriptor.class)
                .route()
                .process((Exchange exch) -> {
                    handleFetchServiceCongigById(exch);
                })
                .endRest();

        rest("/v1/jobs").description("Job execution and management")
                //
                // list the jobs
                .get("/").description("List the jobs for the current user")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .outType(List.class)
                .route()
                .log("Getting jobs")
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
                .post("/{service}").description("Submit a new job")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .outType(JobStatus.class)
                .route()
                .log("Handling Job posting")
//                .process((Exchange exch) -> {
//                    Message in = exch.getIn();
//                    String body = in.getBody(String.class);
//                    LOG.info("BODY:\n" + body);
//                    in.setBody(body);
//                })
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

    private void handleFetchSwagger(Exchange exch, String format) throws IOException {

        Message message = exch.getIn();

        Collection sds = jobManager.fetchServiceDescriptors();
        ServiceDescriptorToOpenAPIConverter converter = createConverter(message);
        OpenAPI oai = converter.convertToOpenApi(sds);

        String result;
        String contentType;
        if ("yaml".equalsIgnoreCase(format)) {
            result = converter.openApiToYaml(oai);
            contentType = CommonMimeTypes.MIME_TYPE_YAML;
        } else {
            // anything else use JSON
            result = converter.openApiToJson(oai);
            contentType = CommonMimeTypes.MIME_TYPE_JSON;
        }
        message.setHeader("Content-Type", contentType);
        message.setBody(result);
    }


    /**
     * Create the ServiceDescriptorToOpenAPIConverter configured to point to the Job executor REST API.
     * <p>
     * This is quite a hacky way of defining the location of the REST services.
     * There may be better ways.
     *
     * @param message
     * @return
     * @throws IOException
     */
    private ServiceDescriptorToOpenAPIConverter createConverter(Message message) throws IOException {
        String server;
        String path;

        LOG.info("JOBS_SERVER: " + JOBS_SERVER);

        if (JOBS_SERVER == null || JOBS_SERVER.isEmpty()) {

            // Here we sniff out the server and path that the services are exposed as.
            // This relies on Tomcat/Camel providing the right information, and this usually requires
            // the proxy settings in the

            String url = message.getHeader("CamelHttpUrl", String.class);
            String uri = message.getHeader("CamelHttpUri", String.class);
            String host = message.getHeader("host", String.class);
            LOG.info("CamelHttpUrl: " + url + " CamelHttpUri: " + uri + " host: " + host);

            if (url.startsWith("https://")) {
                server = "https://" + host;
            } else if (url.startsWith("http://")) {
                server = "http://" + host;
            } else {
                throw new IOException("Unexpected request URL: " + url);
            }
            int loc = uri.indexOf("/rest/v1/swagger");
            LOG.finer("LOC: " + loc);
            path = uri.substring(0, loc + 5);
        } else {
            server = JOBS_SERVER;
            path = JOBS_PATH;
        }
        LOG.info("ServiceDescriptorToOpenAPIConverter config: server=" + server + " path=" + path);
        Collection sds = jobManager.fetchServiceDescriptors();
        return new ServiceDescriptorToOpenAPIConverter(server, path);
    }

    private void handleFetchServiceDescriptorInfo(Exchange exch) throws IOException {
        Message message = exch.getIn();
        List<Map<String, String>> info = jobManager.fetchServiceDescriptorInfo();
        String json = JsonHandler.getInstance().objectToJson(info);
        message.setBody(json);
    }

    private void handleFetchServiceCongigById(Exchange exch) throws IOException {
        Message message = exch.getIn();
        String id = message.getHeader("id", String.class);
        if (id == null || id.isEmpty()) {
            handle500Error(message, "Service ID must be specified", null);
            return;
        }
        ServiceConfig sd = jobManager.fetchServiceConfig(id);
        if (sd == null) {
            handle404Error(message, "Service " + id + " not found");
            return;
        }
        String json = JsonHandler.getInstance().objectToJson(sd);
        message.setBody(json);
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
        } catch (Exception e) {
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

//    private void handleJobSubmitParts(Exchange exch) {
//        Message message = exch.getIn();
//        String service = message.getHeader("service", String.class);
//        LOG.info("Submitting async job for " + service);
//
//        HttpServletRequest req = message.getBody(HttpServletRequest.class);
//        int i = 1;
//        try {
//            Collection<Part> parts = req.getParts();
//            for (Part part : parts) {
//                LOG.info("Part " + i + " " + part.getName() + " " + part.getContentType());
//            }
//            message.setBody("{\"OK\"}");
//            message.setHeader(Exchange.HTTP_RESPONSE_CODE, 201); // created
//        } catch (IOException | ServletException e) {
//            handle500Error(message, "Failed to read parts", e);
//        }
//    }
//
//
//    private void handleJobSubmitFileUpload(Exchange exch) {
//        Message message = exch.getIn();
//        String service = message.getHeader("service", String.class);
//        LOG.info("Submitting async job for " + service);
//
//        HttpServletRequest req = message.getBody(HttpServletRequest.class);
//
//        try {
//            ServletFileUpload servletFileUpload = new ServletFileUpload();
//            FileItemIterator iter = servletFileUpload.getItemIterator(req);
//            int i = 1;
//            while (iter.hasNext()) {
//                FileItemStream fis = iter.next();
//                String contentType = fis.getContentType();
//                String fieldName = fis.getFieldName();
//                String name = fis.getName();
//                FileItemHeaders headers = fis.getHeaders();
//                StringBuilder b = new StringBuilder("Part" + i);
//                b.append(" ContentType=").append(contentType)
//                        .append(" FieldName=").append(fieldName)
//                        .append(" Name=").append(name)
//                        .append(" Headers:");
//                Iterator<String> headerIter = headers.getHeaderNames();
//                while (headerIter.hasNext()) {
//                    String headerName = headerIter.next();
//                    String headerValue = headers.getHeader(headerName);
//                    b.append(headerName).append("->").append(headerValue);
//                }
//                try (InputStream is = fis.openStream()) {
//                    byte[] bytes = IOUtils.convertStreamToBytes(is);
//                    b.append(" Length=").append(bytes.length).append("\n");
//                }
//                LOG.info(b.toString());
//            }
//            message.setBody("{\"OK\"}");
//            message.setHeader(Exchange.HTTP_RESPONSE_CODE, 201); // created
//
//        } catch (FileUploadException | IOException e) {
//            handle500Error(message, "Failed to read parts", e);
//        }
//
//    }

    private void handleJobSubmit(Exchange exch) {

        Message message = exch.getIn();
        String service = message.getHeader("service", String.class);
        LOG.info("Submitting async job for " + service);
        try {
            String username = fetchUsername(message);

            if (service == null || service.isEmpty()) {
                handle500Error(message, "Service not specified", null);
                return;
            }

            Map<String, Object> options = null;
            Map<String, DataSource> inputs = new HashMap<>();

//            dumpMapValues(message.getHeaders(), Level.INFO, "Body headers: ");

            String bHeader = message.getHeader("Content-Disposition", String.class);
            String bFieldName = IOUtils.readPropertyFromHttpHeader("name", bHeader);

            LOG.info("Reading options from body");
            String body = message.getBody(String.class);
            options = JsonHandler.getInstance().objectFromJson(body, new TypeReference<Map<String, Object>>() {
            });


            LOG.info("Found " + message.getAttachments().size() + " attachments");
            Map<String, Attachment> attachments = message.getAttachmentObjects();
            for (Map.Entry<String, Attachment> e : attachments.entrySet()) {
                String key = e.getKey();
                Attachment a = e.getValue();
                String aHeader = a.getHeader("Content-Disposition");
                String aFieldName = IOUtils.readPropertyFromHttpHeader("name", aHeader);

                if (aFieldName == null) {
                    LOG.warning("Content-disposition header did not contain field name. Defaulting to the attachment key which may not be correct.");
                    aFieldName = key;
                } else {
                    LOG.info("Field name for attachment is " + aFieldName);
                }
                DataHandler dataHandler = a.getDataHandler();
                DataSource dataSource = dataHandler.getDataSource();
                inputs.put(aFieldName, dataSource);
                LOG.info("Found DataSource for field " + aFieldName + " of ContentType " + dataSource.getContentType());
            }

            JobStatus jobStatus = jobManager.executeAsync(username, service, options, inputs);

            LOG.info("Job " + jobStatus.getJobId() + " had been submitted");
            String json = JsonHandler.getInstance().objectToJson(jobStatus);
            message.setBody(json);
            message.setHeader(Exchange.HTTP_RESPONSE_CODE, 201); // created
        } catch (AuthenticationException e) {
            handle401Error(message);
        } catch (Exception e) {
            handle500Error(message, "Failed to submit job", e);
        }
    }

//    private String readFormFieldName(Attachment attachment) {
//        String header = attachment.getHeader("Content-Disposition");
//        String result = null;
//        if (header != null) {
//            Map<String, String> values = IOUtils.parseHttpHeader(header);
//            result = values.get("name");
//        }
//        return result;
//    }

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
        Map<String, List<SquonkDataSource>> outputs = jobManager.getJobResultsAsDataSources(username, jobId);
        LOG.fine("Found " + outputs.size() + " outputs");

        for (Map.Entry<String, List<SquonkDataSource>> e : outputs.entrySet()) {
            for (SquonkDataSource dataSource : e.getValue()) {
                String name = dataSource.getName() == null ? dataSource.getRole() : dataSource.getName();
                if (name == null) {
                    name = "unknown";
                }
                if (outputs.size() == 1) {
                    name = e.getKey() + "_" + name;
                }
                LOG.fine("Adding attachment " + name + " of type " + dataSource.getContentType());
                dataSource.setGzipContent(false);

                // TODO - consider if we can handle conversions - difficult if there are multiple outputs?

                // we need to set the Content-Transfer-Encoding header otherwise the DatasetMetadata gets base64 encoded.
                // It's unclear at this stage what the best option is so we let this be configured.
                DefaultAttachment attachment = new DefaultAttachment(dataSource);
                attachment.setHeader("Content-Transfer-Encoding", CONTENT_TRANSFER_ENCODING);

                message.addAttachmentObject(name, attachment);
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

    private void dumpMapValues(Map map, Level level, String prefix) {
        StringBuilder b = new StringBuilder(prefix);
        map.forEach((k, v) -> {
            b.append(k.toString()).append("->").append(v == null ? "null" : v.toString()).append("\n");
        });
        LOG.log(level, b.toString());
    }

}
