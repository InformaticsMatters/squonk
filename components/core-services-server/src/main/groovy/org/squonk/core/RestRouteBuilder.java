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

package org.squonk.core;

import org.apache.camel.Message;
import org.squonk.config.SquonkServerConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition;
import org.squonk.jobdef.ExternalJobDefinition;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.JobStatus;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.client.JobStatusClient;
import org.squonk.core.service.discovery.ServiceDiscoveryRouteBuilder;
import org.squonk.core.service.job.Job;
import org.squonk.core.service.job.MemoryJobStatusClient;
import org.squonk.core.service.job.PostgresJobStatusClient;
import org.squonk.core.service.job.StepsCellJob;
import org.squonk.core.service.notebook.NotebookPostgresClient;
import org.squonk.core.service.user.UserHandler;
import org.squonk.core.user.User;
import org.squonk.core.util.Utils;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import org.squonk.notebook.api.NotebookSavepointDTO;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.camel.model.rest.RestParamType.*;
import static org.squonk.client.VariableClient.VarType;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_USERS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_USERS_EXCHANGE_PARAMS;

/** REST services for coreservices.
 *
 * The base URL is typically http://coreservices:8080/coreservices/rest but could be different if non-standard settings
 * are used.
 *
 * The following endpoints are defined (relative to that base path):
 *
 * GET /ping
 * A simple health check URL. Returns the string 'OK'
 *
 * POST /echo
 * Simple services that returns the POSTed content as the response. Useful for testing.
 *
 * GET /v1/services
 * Get a list of all the @{link ServiceDescriptor}s that have been registered
 *
 * POST /v1/services
 * Post a new ServiceDescriptor or update an existing one. The Content-Type header MUST be specified and can have one of
 * these values:
 * <ul>
 * <li>application/x-squonk-service-descriptor-set+{json|yaml}: The content is expected to be a @{link ServiceDescriptorSet}
 * serialised as json or yaml</li>
 * <li>application/x-squonk-service-descriptor-docker+{json|yaml}: The content is expected to be a single
 * @{link DockerServiceDescriptor} serialised as json or yaml and a header named Base-URL that defines the base
 * URL of the ServiceDescriptorSet that the service descriptor belongs to MUST be present</li>
 * <li>application/x-squonk-service-descriptor-nextflow+{json|yaml}: The content is expected to be a single
 *  @{link NextflowServiceDescriptor} serialised as json or yaml and a header named Base-URL that defines the base
 *  URL of the ServiceDescriptorSet that the service descriptor belongs to MUST be present</li>
 * </ul>
 *
 * GET /v1/jobs
 * Get the status of all jobs
 *
 * POST /v1/jobs
 * Create a new job
 *
 * GET /v1/jobs/{id}
 * Get the status of a particular job
 *
 * POST /v1/jobs/{id}
 * Update the status of a particular job
 *
 * GET /v1/notebooks
 * Get the notebooks for the current user
 *
 * POST /v1/notebooks
 * Create a new notebook
 *
 * DELETE /v1/notebooks/{notebookid}
 * Delete a notebook
 *
 * PUT /v1/notebooks/{notebookid}
 * Update the name and description of a notebook
 *
 * GET /v1/notebooks/{notebookid}/e
 * Get the NotebookEditables for a particular notebook
 *
 * GET /v1/notebooks/{notebookid}/s
 * Get the savepoints for a particular notebook
 *
 * POST /v1/notebooks/{notebookid}/e
 * Create a new editable for a notebook
 *
 * PUT /v1/notebooks/{notebookid}/e/{editableid}
 * Update an editable for a noebook
 *
 * POST /v1/notebooks/{notebookid}/s
 * Create a new savepoint for a notebook
 *
 * PUT /v1/notebooks/{notebookid}/s/{savepointid}/description
 * Update the description of a savepoint
 *
 * GET /v1/notebooks/{notebookid}/v/{sourceid}/{cellid}/{varname}/{type}/{key}
 * Read a variable value using either its label or its editable/savepoint id
 *
 * POST /v1/notebooks/{notebookid}/v/{editableid}/{cellid}/{varname}/{type}/{key}
 * Write a variable value
 *
 * DELETE /v1/notebooks/{notebookid}/v/{editableid}/{cellid}/{varname}
 * Delete a variable
 *
 * GET /v1/users/SquonkUsername
 * Get the User object for a user
 *
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder implements ServerConstants {

    private static final Logger LOG = Logger.getLogger(RestRouteBuilder.class.getName());

    private static final String NOTEBOOKID = "notebookid";
    private static final String SAVEPOINTID = "savepointid";
    private static final String SOURCEID = "sourceid";
    private static final String USER = "user";
    private static final String PARENT = "parent";
    private static final String EDITABLEID = "editableid";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final String CELLID = "cellid";
    private static final String VARNAME = "varname";
    private static final String KEY = "key";
    private static final String TYPE = "type";
    private static final String LAYER = "layer";
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";

    private final JobStatusClient jobstatusClient;
    private final NotebookPostgresClient notebookClient;
    private final MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials();
    private final String userNotifyMqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_USERS_EXCHANGE_NAME, MQUEUE_USERS_EXCHANGE_PARAMS);


    public RestRouteBuilder() {
        String server = SquonkServerConfig.SQUONK_DB_SERVER;
        if (server == null) {
            LOG.info("Using MemoryJobStatusClient");
            jobstatusClient = MemoryJobStatusClient.INSTANCE;
            notebookClient = null;
        } else {
            LOG.info("Using Postgres Clients");
            jobstatusClient = PostgresJobStatusClient.INSTANCE;
            notebookClient = NotebookPostgresClient.INSTANCE;
        }
    }

    private void handleError(Exchange exch, String responseCode, String errorMessage) {
        handleError(exch, responseCode, errorMessage, null);
    }

    private void handleError(Exchange exch, String responseCode, String errorMessage, Throwable t) {
        Message m;
        if (exch.hasOut()) {
            m = exch.getOut();
        } else {
            m = exch.getIn();
        }
        m.setHeader(Exchange.HTTP_RESPONSE_CODE, responseCode);
        StringBuilder b = new StringBuilder(errorMessage).append("\n");
        if (t == null) {
            //b.append("No more details are available.\n");
        } else if (t.getMessage() == null) {
            b.append("No message provided\n").append("\nCause is:\n\n");
            b.append(t.toString());
        } else {
            b.append("Message: ").append(t.getMessage()).append("\nCause is:\n\n");
            b.append(t.toString());
        }
        m.setBody(b.toString());
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        onException(IllegalStateException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant(TEXT_PLAIN))
                .process((Exchange exch) -> {
                    Throwable caused = exch.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                    if (caused == null || caused.getMessage() == null) {
                        exch.getIn().setBody("Invalid state: Cause unknown");
                    } else {
                        exch.getIn().setBody("Invalid state: " + caused.getMessage());
                    }
                });

        onException(NotFoundException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.CONTENT_TYPE, constant(TEXT_PLAIN))
                .process((Exchange exch) -> {
                    Throwable caused = exch.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                    if (caused == null || caused.getMessage() == null) {
                        LOG.warning("404 error: Cause unknown");
                        exch.getIn().setBody("Not Found: Cause unknown");
                    } else {
                        LOG.warning("404 error: " + caused.getMessage());
                        exch.getIn().setBody("Not Found: " + caused.getMessage());
                    }
                });

        /* These are the REST endpoints - exposed as public web services
         */
        rest("/ping")
                .get().description("Simple ping service to check things are running")
                .produces(TEXT_PLAIN)
                .route()
                .transform(constant("OK\n")).endRest();

        rest("/echo")
                .post().description("Simple echo service for testing")
                .bindingMode(RestBindingMode.off)
                .route()
                .process((Exchange exch) -> {
                    Object body = exch.getIn().getBody();
                    LOG.log(Level.INFO, "Echoing: {0}", (body == null ? "null" : body.getClass().getName()));
                    exch.getIn().setBody(body);
                })
                .endRest();

        rest("/v1/services")
                // GET the ServiceConfigs
                // TODO - This is a duplicate of the next definition. Remove this one once refactoring is complete
                .get().description("Get service config definitions for the available services")
                .bindingMode(RestBindingMode.json)
                .outType(HttpServiceDescriptor.class)
                .produces(APPLICATION_JSON)
                .route()
                .to(ServiceDiscoveryRouteBuilder.ROUTE_REQUEST_SERVICE_CONFIGS)
                .endRest()
                // GET the ServiceConfigs
                .get("/configs").description("Get service config definitions for the available services")
                .bindingMode(RestBindingMode.json)
                .outType(ServiceConfig.class)
                .produces(APPLICATION_JSON)
                .route()
                .to(ServiceDiscoveryRouteBuilder.ROUTE_REQUEST_SERVICE_CONFIGS)
                .endRest()
                // GET the ServiceDescriptors
                .get("/descriptors").description("Get service descriptor definitions for the available services")
                .bindingMode(RestBindingMode.off)
                .outType(ServiceDescriptor.class)
                .produces(APPLICATION_JSON)
                .route()
                .to(ServiceDiscoveryRouteBuilder.ROUTE_REQUEST_SERVICE_DESCRIPTORS)
                .process((Exchange exch) -> {
                    // this is necessary as Jackson does not serialize List<ServiceDescriptor> correctly.
                    // The "@class": "org.squonk.core.HttpServiceDescriptor" property is missing.
                    List<ServiceDescriptor> sds = exch.getIn().getBody(List.class);
                    StringBuffer buf = new StringBuffer("[");
                    int count = 0;
                    for (ServiceDescriptor sd : sds) {
                        if (count > 0) {
                            buf.append(",");
                        }
                        String json = JsonHandler.getInstance().objectToJson(sd);
                        buf.append(json);
                        count++;
                    }
                    buf.append("]");
                    String result = buf.toString();
                    exch.getIn().setBody(result);
                })
                .endRest()
                // POST new service descriptors
                .post().description("Post ServiceDescriptors for a set of available services")
                .produces(TEXT_PLAIN)
                .route()
                .choice()
                // multipart/mixed - a single SD that comes in parts and needs assembling
                // Header "Base-URL" that defines the URL of the ServiceDescriptorSet must be present
                .when(header(Exchange.CONTENT_TYPE).startsWith(CommonMimeTypes.MIME_TYPE_MULTIPART_MIXED))
                .log("Dispatching multipart")
                .unmarshal().mimeMultipart()
                .to(ServiceDiscoveryRouteBuilder.ROUTE_POST_SD_SINGLE)
                // A ServiceDescriptorSet: application/x-squonk-service-descriptor-set+json/yaml
                .when(header(Exchange.CONTENT_TYPE).startsWith(CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_SET))
                .log("Dispatching ServiceDescriptorSet")
                .to(ServiceDiscoveryRouteBuilder.ROUTE_POST_SD_SET)
                // Probably something like application/x-squonk-service-descriptor-docker+json
                // A single complete SD where the header "Base-URL" must be present
                .when(header(Exchange.CONTENT_TYPE).startsWith(CommonMimeTypes.SERVICE_DESCRIPTOR_BASE))
                .log("Dispatching single ServiceDescriptor")
                .to(ServiceDiscoveryRouteBuilder.ROUTE_POST_SD_SINGLE)
                .otherwise()
                .transform().simple("Unsupported Content-Type: ${header['Content-Type']}")
                .endChoice()
                .endRest();


        rest("/v1/jobs").description("Job submission and status services")
                //
                // GET statuses
                // TODO - handle filter criteria
                .get("/").description("Get the statuses of all jobs")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(jobstatusClient.list(null));
                })
                .endRest()
                //
                // getServiceDescriptors a particular status
                .get("/{id}").description("Get the latest status of an individual job")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    String id = exch.getIn().getHeader("id", String.class);
                    exch.getIn().setBody(jobstatusClient.get(id));
                })
                .endRest()
                //
                // submit new job
                .post("/").description("Submit a new job")
                .bindingMode(RestBindingMode.off).produces(APPLICATION_JSON)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    String json = exch.getIn().getBody(String.class);
                    LOG.fine("JSON is " + json);
                    JobDefinition jobdef = JsonHandler.getInstance().objectFromJson(json, JobDefinition.class);
                    LOG.info("Received request to submit job for " + jobdef);
                    Integer count = exch.getIn().getHeader(HEADER_JOB_SIZE, Integer.class);
                    if (jobdef instanceof ExecuteCellUsingStepsJobDefinition) {
                        ExecuteCellUsingStepsJobDefinition stepsJopbDef = (ExecuteCellUsingStepsJobDefinition) jobdef;
                        Job job = new StepsCellJob(jobstatusClient, stepsJopbDef);
                        LOG.info("Starting Job");
                        JobStatus result = job.start(exch.getContext(), Utils.fetchUsername(exch), count);
                        LOG.info("Job " + result.getJobId() + " started");
                        String jsonResult = JsonHandler.getInstance().objectToJson(result);
                        exch.getIn().setBody(jsonResult);
                    } else if (jobdef instanceof ExternalJobDefinition) {
                        ExternalJobDefinition externalJob = (ExternalJobDefinition) jobdef;
                        LOG.info("Creating job with ID " + externalJob.getJobId());
                        JobStatus result = jobstatusClient.create(externalJob, Utils.fetchUsername(exch), count);
                        LOG.fine("Job " + result.getJobId() + " created");
                        String jsonResult = JsonHandler.getInstance().objectToJson(result);
                        exch.getIn().setBody(jsonResult);
                    } else {
                        throw new IllegalStateException("Job definition type " + jobdef.getClass().getName() + " not currently supported");
                    }
                })
                .inOnly("seda:notifyJobStatusUpdate")
                .endRest()
                //
                // update
                .post("/{id}").description("Update the status of a job")
                .bindingMode(RestBindingMode.off).produces(APPLICATION_JSON)
                .outType(JobStatus.class)
                .route()
                .log("Updating status of job ${header.id} to status ${header.Status} and processed count of ${header.ProcessedCount}")
                .process((Exchange exch) -> {
                    Message message = exch.getIn();
                    String id = message.getHeader("id", String.class);
                    String status = message.getHeader(HEADER_JOB_STATUS, String.class);
                    Integer processedCount = message.getHeader(HEADER_JOB_PROCESSED_COUNT, Integer.class);
                    Integer errorCount = message.getHeader(HEADER_JOB_ERROR_COUNT, Integer.class);
                    JobStatus result;
                    if (status == null) {
                        result = jobstatusClient.incrementCounts(id, processedCount, errorCount);
                    } else {
                        String event = exch.getIn().getBody(String.class);
                        result = jobstatusClient.updateStatus(id, JobStatus.Status.valueOf(status), event, processedCount, errorCount);
                    }
                    if (result == null) {
                        message.setBody("{\"error\": \"Job " + id + " not found\"}");
                        message.setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                    } else {
                        message.setHeader(HEADER_SQUONK_USERNAME, result.getUsername());
                        String jsonResult = JsonHandler.getInstance().objectToJson(result);
                        message.setBody(jsonResult);
                    }
                })
                .inOnly("seda:notifyJobStatusUpdate")
                .endRest();

        rest("/v1/notebooks").description("Notebook services")
                //
                .get("/").description("Get all notebooks for the user")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookDTO.class)
                .param().name(USER).type(query).description("The username").dataType("string").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    String user = exch.getIn().getHeader(USER, String.class);
                    checkNotNull(user, "Username must be specified");
                    List<NotebookDTO> results = notebookClient.listNotebooks(user);
                    exch.getIn().setBody(results);
                })
                .endRest()
                //
                .get("/{notebookid}/e").description("List the editables for a notebook for the user")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookEditableDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(USER).type(query).description("The username").dataType("string").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    String user = exch.getIn().getHeader(USER, String.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(user, "Username must be specified");
                    List<NotebookEditableDTO> results = notebookClient.listEditables(notebookid, user);
                    exch.getIn().setBody(results);
                })
                .endRest()
                //
                .get("/{notebookid}/s").description("List all savepoints for a notebook")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookSavepointDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    List<NotebookSavepointDTO> results = notebookClient.listSavepoints(notebookid);
                    exch.getIn().setBody(results);
                })
                .endRest()
                //
                .post("/").description("Create a new notebook")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookDTO.class)
                .param().name(USER).type(query).description("The owner of the new notebook").dataType("string").required(true).endParam()
                .param().name(NAME).type(query).description("The name for the new notebook").dataType("string").required(true).endParam()
                .param().name(DESCRIPTION).type(query).description("A description for the new notebook").dataType("string").required(false).endParam()
                .route()
                .process((Exchange exch) -> {
                    String user = exch.getIn().getHeader(USER, String.class);
                    String name = exch.getIn().getHeader(NAME, String.class);
                    String description = exch.getIn().getHeader(DESCRIPTION, String.class);
                    checkNotNull(user, "Username must be specified");
                    checkNotNull(name, "Notebook name must be specified");
                    NotebookDTO result = notebookClient.createNotebook(user, name, description);
                    LOG.info("Created notebook " + result.getId());
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                .delete("/{notebookid}").description("Delete a notebook")
                .bindingMode(RestBindingMode.off)
                .produces(TEXT_PLAIN)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    LOG.info("Deleting notebook " + notebookid);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    boolean result = notebookClient.deleteNotebook(notebookid);
                    exch.getIn().setBody(null);
                    if (result) {
                        exch.getIn().setBody("OK");
                    } else {
                        String msg = "Notebook " + notebookid + " could not be deleted. May not exist or may not be yours?";
                        LOG.warning(msg);
                        handleError(exch, "404", msg);
                    }
                })
                .endRest()
                //
                .get("/{notebookid}/layer").description("List the layers a notebook belongs to")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(List.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    List<String> results = notebookClient.listLayers(notebookid);
                    exch.getIn().setBody(results);
                })
                .endRest()
                //
                .post("/{notebookid}/layer/{layer}").description("Add notebook to this layer")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(LAYER).type(path).description("The name of the layer").dataType("string").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    String layer = exch.getIn().getHeader(LAYER, String.class);
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(layer, "Layer name must be specified");
                    NotebookDTO result = notebookClient.addNotebookToLayer(notebookid, layer);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                .delete("/{notebookid}/layer/{layer}").description("Remove notebook from this layer")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(LAYER).type(path).description("The name of the layer").dataType("string").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    String layer = exch.getIn().getHeader(LAYER, String.class);
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    checkNotNull(layer, "Layer name must be specified");
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    NotebookDTO result = notebookClient.removeNotebookFromLayer(notebookid, layer);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                .post("/{notebookid}/e").description("Create a new editable for a notebook")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookEditableDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(USER).type(query).description("The owner of the new notebook").dataType("string").required(true).endParam()
                .param().name(PARENT).type(query).description("The parent savepoint").dataType("long").required(false).endParam()
                .route()
                .process((Exchange exch) -> {
                    String user = exch.getIn().getHeader(USER, String.class);
                    Long parent = exch.getIn().getHeader(PARENT, Long.class);
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(user, "Username must be specified");
                    NotebookEditableDTO result = notebookClient.createEditable(notebookid, parent, user);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                .put("/{notebookid}/e/{editableid}").description("Update the definition of an editable")
                //.bindingMode(RestBindingMode.json)
                .bindingMode(RestBindingMode.off)
                .consumes(APPLICATION_JSON).produces(APPLICATION_JSON)
                .type(NotebookCanvasDTO.class)
                .outType(NotebookEditableDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(EDITABLEID).type(path).description("Editable ID").dataType("long").required(true).endParam()
                .param().name("json").type(body).description("Content (as JSON)").dataType("string").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    Long editableid = exch.getIn().getHeader(EDITABLEID, Long.class);
                    //NotebookCanvasDTO canvasDTO = exch.getIn().getBody(NotebookCanvasDTO.class);
                    String json1 = exch.getIn().getBody(String.class);
                    NotebookCanvasDTO canvasDTO = JsonHandler.getInstance().objectFromJson(json1, NotebookCanvasDTO.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(editableid, "Editable ID must be specified");
                    checkNotNull(canvasDTO, "Canvas definition must be specified as body");
                    NotebookEditableDTO result = notebookClient.updateEditable(notebookid, editableid, canvasDTO);
                    String json2 = JsonHandler.getInstance().objectToJson(result);
                    exch.getIn().setBody(json2);
                    //exch.getIn().setBody(result);
                })
                .endRest()
                .delete("/{notebookid}/e/{editableid}").description("Delete an editable")
                .bindingMode(RestBindingMode.off)
                .produces(TEXT_PLAIN)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(EDITABLEID).type(path).description("Editable ID").dataType("long").required(true).endParam()
                .param().name(USER).type(query).description("The owner of the editable").dataType("string").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    Long editableid = exch.getIn().getHeader(EDITABLEID, Long.class);
                    String user = exch.getIn().getHeader(USER, String.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(editableid, "Editable ID must be specified");
                    checkNotNull(user, "Username must be specified");
                    boolean result = notebookClient.deleteEditable(notebookid, editableid, user);
                    if (result) {
                        exch.getIn().setBody("OK");
                    } else {
                        String msg = "Editable " + editableid + " could not be deleted. May not exist or may not be yours?";
                        LOG.warning(msg);
                        handleError(exch, "404", msg);
                    }
                })
                .endRest()
                //
                .post("/{notebookid}/s").description("Create a new savepoint for a notebook")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookEditableDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(EDITABLEID).type(query).description("The editable ID to make the savepoint from").dataType("long").required(true).endParam()
                .param().name(DESCRIPTION).type(query).description("The description of the savepoint").dataType("string").required(false).endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    Long editableid = exch.getIn().getHeader(EDITABLEID, Long.class);
                    String description = exch.getIn().getHeader(DESCRIPTION, String.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(editableid, "Editable ID must be specified");
                    NotebookEditableDTO result = notebookClient.createSavepoint(notebookid, editableid, description);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                .put("/{notebookid}/s/{savepointid}/description").description("Update the description of a savepoint")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookSavepointDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(SAVEPOINTID).type(path).description("Savepoint ID").dataType("long").required(true).endParam()
                .param().name(DESCRIPTION).type(query).description("New description").dataType("string").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    String description = exch.getIn().getHeader(DESCRIPTION, String.class);
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    Long savepointid = exch.getIn().getHeader(SAVEPOINTID, Long.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(savepointid, "Savepoint ID must be specified");
                    checkNotNull(description, "Description must be specified");
                    NotebookSavepointDTO result = notebookClient.setSavepointDescription(notebookid, savepointid, description);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                .put("/{notebookid}/s/{savepointid}/label").description("Update the label of a savepoint")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookSavepointDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(SAVEPOINTID).type(path).description("Savepoint ID").dataType("long").required(true).endParam()
                .param().name("label").type(query).description("New Label").dataType("string").required(false).endParam()
                .route()
                .process((Exchange exch) -> {
                    String label = exch.getIn().getHeader("label", String.class);
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    Long savepointid = exch.getIn().getHeader(SAVEPOINTID, Long.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(savepointid, "Savepoint ID must be specified");
                    NotebookSavepointDTO result = notebookClient.setSavepointLabel(notebookid, savepointid, label);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                .put("/{notebookid}").description("Update the name and description of a notebook")
                .bindingMode(RestBindingMode.json).produces(APPLICATION_JSON)
                .outType(NotebookDTO.class)
                .param().name(NOTEBOOKID).type(path).description("Notebook ID").dataType("long").required(true).endParam()
                .param().name(NAME).type(query).description("New name").dataType("string").required(true).endParam()
                .param().name(DESCRIPTION).type(query).description("New description").dataType("string").required(false).endParam()
                .route()
                .process((Exchange exch) -> {
                    String name = exch.getIn().getHeader(NAME, String.class);
                    String description = exch.getIn().getHeader(DESCRIPTION, String.class);
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    checkNotNull(notebookid, "Notebook ID must be specified");
                    checkNotNull(name, "Notebook name must be specified");
                    NotebookDTO result = notebookClient.updateNotebook(notebookid, name, description);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                // VARIABLES
                //
                // GET
                .get("/{notebookid}/v/{sourceid}/{cellid}/{varname}/{type}/{key}").description("Read a variable value using either its label or its editable/savepoint id")
                .bindingMode(RestBindingMode.off)
                .param().name(NOTEBOOKID).type(path).description("The notebook ID").dataType("long").required(true).endParam()
                .param().name(SOURCEID).type(path).description("The editable/savepoint ID").dataType("long").required(true).endParam()
                .param().name(CELLID).type(path).description("The cell ID").dataType("long").required(true).endParam()
                .param().name(VARNAME).type(path).description("The name of the variable").dataType("string").required(true).endParam()
                .param().name(KEY).type(path).description("Optional key for the variable. If not provide key of 'default' is assumed").dataType("string").required(false).endParam()
                .param().name(TYPE).type(path).description("The type of variable (s = stream, t = text)").dataType("string").required(true).allowableValues("s", "t").endParam()
                //.param().name("label").type(query).description("The label of the variable").dataType("string").required(false).endParam()
                .route()
                .process((Exchange exch) -> {
                    String varname = exch.getIn().getHeader(VARNAME, String.class);
                    String key = exch.getIn().getHeader(KEY, String.class);
                    VarType type = VarType.valueOf(exch.getIn().getHeader(TYPE, String.class));
                    Long sourceid = exch.getIn().getHeader(SOURCEID, Long.class);
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    Long cellid = exch.getIn().getHeader(CELLID, Long.class);

                    if (notebookid == null) {
                        handleError(exch, "500", "Notebook ID not specified");
                        return;
                    }
                    if (sourceid == null) {
                        handleError(exch, "500", "Source ID not specified");
                        return;
                    }
                    if (cellid == null) {
                        handleError(exch, "500", "Cell ID not specified");
                        return;
                    }
                    if (varname == null) {
                        handleError(exch, "500", "Variable name not specified");
                        return;
                    }
                    if (type == null) {
                        handleError(exch, "500", "Variable type not specified");
                        return;
                    }
                    // TODO -set the mime type and encoding
                    // TODO - distinguish between a variable that is not present and one that has no value
                    switch (type) {
                        case s:
                            InputStream is = notebookClient.readStreamValue(notebookid, sourceid, cellid, varname, key);
                            //log.info("Stream Variable: " + is);
                            if (is == null) {
                                String.format("Stream variable %s:%s for %s:%s:%s not found", varname, key, notebookid, sourceid, cellid);
                            } else {
                                exch.getIn().setBody(is);
                            }
                            break;
                        case t:
                            String t = notebookClient.readTextValue(notebookid, sourceid, cellid, varname, key);
                            //log.info("String Variable: " + t);
                            if (t == null) {
                                handleError(exch, "404",
                                        String.format("Text variable %s:%s for %s:%s:%s not found", varname, key, notebookid, sourceid, cellid));
                            } else {
                                exch.getIn().setBody(t);
                            }
                            break;
                        default:
                            handleError(exch, "404",
                                    String.format("Invalid variable type. Must be s (stream) or t (text). Found %s", type));
                    }
                })
                .endRest()
                // write a variable
                .post("/{notebookid}/v/{editableid}/{cellid}/{varname}/{type}/{key}").description("Write a variable value")
                .bindingMode(RestBindingMode.off)
                .param().name(NOTEBOOKID).type(path).description("The notebook ID").dataType("long").required(true).endParam()
                .param().name(VARNAME).type(path).description("The name of the variable").dataType("string").required(true).endParam()
                .param().name(KEY).type(path).description("Optional key for the variable. If not provide key of 'default' is assumed").dataType("string").required(false).endParam()
                .param().name(TYPE).type(path).description("The type of variable (s = stream, t = text)").dataType("string").required(true).allowableValues("s", "t").endParam()
                .param().name(EDITABLEID).type(path).description("The editable ID").dataType("long").required(true).endParam()
                .param().name(CELLID).type(path).description("The cell ID that produces the value").dataType("long").required(true).endParam()
                .param().name("body").type(body).description("The value").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    String varname = exch.getIn().getHeader(VARNAME, String.class);
                    String key = exch.getIn().getHeader(KEY, String.class);
                    VarType type = VarType.valueOf(exch.getIn().getHeader(TYPE, String.class));
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    Long editableid = exch.getIn().getHeader(EDITABLEID, Long.class);
                    Long cellid = exch.getIn().getHeader(CELLID, Long.class);

                    if (notebookid == null) {
                        handleError(exch, "500", "Notebook ID not specified");
                        return;
                    }
                    if (editableid == null) {
                        handleError(exch, "500", "Editable ID not specified");
                        return;
                    }
                    if (cellid == null) {
                        handleError(exch, "500", "Cell ID not specified");
                        return;
                    }
                    if (varname == null) {
                        handleError(exch, "500", "Variable name not specified");
                        return;
                    }
                    if (type == null) {
                        handleError(exch, "500", "Variable type not specified");
                        return;
                    }

                    switch (type) {
                        case s:
                            InputStream is = exch.getIn().getBody(InputStream.class);
                            notebookClient.writeStreamValue(notebookid, editableid, cellid, varname, is, key);
                            break;
                        case t:
                            String t = exch.getIn().getBody(String.class);
                            notebookClient.writeTextValue(notebookid, editableid, cellid, varname, t, key);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid variable type. Must be s (stream) or t (text)");
                    }

                })
                .endRest()
                .delete("/{notebookid}/v/{editableid}/{cellid}/{varname}").description("Delete a variable")
                .bindingMode(RestBindingMode.off)
                .param().name(NOTEBOOKID).type(path).description("The notebook ID").dataType("long").required(true).endParam()
                .param().name(VARNAME).type(path).description("The name of the variable").dataType("string").required(true).endParam()
                .param().name(EDITABLEID).type(path).description("The editable ID").dataType("long").required(true).endParam()
                .param().name(CELLID).type(path).description("The cell ID that produces the value").dataType("long").required(true).endParam().route()
                .process((Exchange exch) -> {
                    String varname = exch.getIn().getHeader(VARNAME, String.class);
                    Long notebookid = exch.getIn().getHeader(NOTEBOOKID, Long.class);
                    Long editableid = exch.getIn().getHeader(EDITABLEID, Long.class);
                    Long cellid = exch.getIn().getHeader(CELLID, Long.class);

                    LOG.info("DELETE: " + notebookid + " " + editableid + " " + cellid + " " + varname);

                    if (notebookid == null) {
                        handleError(exch, "500", "Notebook ID not specified");
                        return;
                    }
                    if (editableid == null) {
                        handleError(exch, "500", "Editable ID not specified");
                        return;
                    }
                    if (cellid == null) {
                        handleError(exch, "500", "Cell ID not specified");
                        return;
                    }
                    if (varname == null) {
                        handleError(exch, "500", "Variable name not specified");
                        return;
                    }

                    notebookClient.deleteVariable(notebookid, editableid, cellid, varname);

                });

        from("seda:notifyJobStatusUpdate")
                .setHeader("rabbitmq.ROUTING_KEY", simple("users.${header[" + HEADER_SQUONK_USERNAME + "]}.jobstatus"))
                //.log("Routing Key: ${header[rabbitmq.ROUTING_KEY]}")
                .inOnly(userNotifyMqueueUrl);


        rest("/v1/users").description("User management services")
                //
                // GET statuses
                .get("/{" + HEADER_SQUONK_USERNAME + "}").description("Get the User object for this username (specified as the query parameter named " + HEADER_SQUONK_USERNAME)
                .bindingMode(RestBindingMode.json)
                .produces(APPLICATION_JSON)
                .produces(APPLICATION_JSON)
                .outType(User.class)
                .route()
                .process((Exchange exch) -> UserHandler.putUser(exch))
                .endRest();
    }

    private void checkNotNull(Object var, String messageForExceptionIfNull) {
        if (var == null) {
            throw new IllegalStateException(messageForExceptionIfNull);
        }
    }

}
