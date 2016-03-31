package org.squonk.core;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.core.dataset.service.DatasetHandler;
import org.squonk.core.service.discovery.ServiceDiscoveryRouteBuilder;
import org.squonk.core.service.job.Job;
import org.squonk.core.service.job.MemoryJobStatusClient;
import org.squonk.core.service.job.PostgresJobStatusClient;
import org.squonk.core.service.job.StepsCellJob;
import org.squonk.core.service.notebook.NotebookPostgresClient;
import org.squonk.core.user.User;
import org.squonk.core.service.user.UserHandler;
import org.squonk.core.util.Utils;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.chemaxon.molecule.MoleculeObjectUtils;
import org.squonk.client.JobStatusClient;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.notebook.api.NotebookDescriptor;
import org.squonk.notebook.api.NotebookEditable;
import org.squonk.notebook.api.NotebookInstance;
import org.squonk.notebook.api.NotebookSavepoint;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.squonk.client.VariableClient.VarType;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_USERS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_USERS_EXCHANGE_PARAMS;

import static org.apache.camel.model.rest.RestParamType.query;
import static org.apache.camel.model.rest.RestParamType.body;
import static org.apache.camel.model.rest.RestParamType.path;

/**
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder implements ServerConstants {

    private static final Logger LOG = Logger.getLogger(RestRouteBuilder.class.getName());

    private final JobStatusClient jobstatusClient;
    private final NotebookPostgresClient notebookClient;
    private final JsonHandler jsonHandler = JsonHandler.getInstance();
    private MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials();
    private final String userNotifyMqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_USERS_EXCHANGE_NAME, MQUEUE_USERS_EXCHANGE_PARAMS);


    public RestRouteBuilder() {
        String server = IOUtils.getConfiguration("SQUONK_DB_SERVER", null);
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

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        onException(IllegalStateException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
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
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .process((Exchange exch) -> {
                    Throwable caused = exch.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                    if (caused == null || caused.getMessage() == null) {
                        exch.getIn().setBody("Not Found: Cause unknown");
                    } else {
                        exch.getIn().setBody("Not Found: " + caused.getMessage());
                    }
                });

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("/ping")
                .get().description("Simple ping service to check things are running")
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();

        rest("/echo")
                .post().description("Simple echo service for testing")
                .bindingMode(RestBindingMode.off)
                //.consumes("application/json")
                //.produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    Object body = exch.getIn().getBody();
                    LOG.log(Level.INFO, "Echoing: {0}", (body == null ? "null" : body.getClass().getName()));
                    exch.getIn().setBody(body);
                })
                .endRest();

        rest("/v1/services")
                .get().description("Get service definitions for the available services")
                .bindingMode(RestBindingMode.json)
                .outType(ServiceDescriptor.class)
                .produces("application/json")
                .to(ServiceDiscoveryRouteBuilder.ROUTE_REQUEST);

        rest("/v1/datasets").description("Dataset management services")
                // POST
                .post()
                .description("Upload file to submit new dataset. File is the body and dataset name is given by the header named " + HEADER_DATAITEM_NAME)
                .bindingMode(RestBindingMode.off)
                .produces("application/json")
                .route()
                // this is a temp hack - client should set Content-Type, but for now we assume SDF
                .setHeader("Content-Type", constant("chemical/x-mdl-sdfile"))
                .to("direct:datasets/upload")
                .endRest()
                // 
                // DELETE
                .delete("/{" + REST_DATASET_ID + "}").description("Deletes the dataset specified by the ID")
                .route()
                .process((Exchange exch) -> DatasetHandler.deleteDataset(exch))
                .transform(constant("OK"))
                .endRest()
                //
                // GET all 
                .get().description("List all datasets")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(DataItem.class)
                .route()
                .process((Exchange exch) -> DatasetHandler.putDataItems(exch))
                .endRest()
                //
                // GET DataItem for one
                .get("/{" + REST_DATASET_ID + "}/dataitem").description("Gets a description of the dataset specified by the ID as JSON")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(DataItem.class)
                .route()
                .process((Exchange exch) -> DatasetHandler.putDataItem(exch))
                .endRest()
                //
                // GET content for item
                .get("/{" + REST_DATASET_ID + "}/content").description("Gets the actual data content specified by the ID as JSON")
                .bindingMode(RestBindingMode.off).produces("application/json")
                .route()
                .process((Exchange exch) -> DatasetHandler.putJsonForDataset(exch))
                .setBody(simple("${body.inputStream}"))
                .endRest();


        rest("/v1/jobs").description("Job submission and status services")
                //
                // GET statuses
                // TODO - handle filter criteria
                .get("/").description("Get the statuses of all jobs")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(jobstatusClient.list(null));
                })
                .endRest()
                //
                // get a particular status
                .get("/{id}").description("Get the latest status of an individual job")
                .bindingMode(RestBindingMode.json).produces("application/json")
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
                .bindingMode(RestBindingMode.off).produces("application/json")
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    String json = exch.getIn().getBody(String.class);
                    LOG.info("JSON is " + json);
                    JobDefinition jobdef = JsonHandler.getInstance().objectFromJson(json, JobDefinition.class);
                    LOG.info("Received request to submit job for " + jobdef);

                    Integer count = exch.getIn().getHeader(HEADER_JOB_SIZE, Integer.class);
                    Job job = null;
                    if (jobdef instanceof ExecuteCellUsingStepsJobDefinition) {
                        job = new StepsCellJob(jobstatusClient, (ExecuteCellUsingStepsJobDefinition) jobdef);
                    } else {
                        throw new IllegalStateException("Job definition type " + jobdef.getClass().getName() + " not currently supported");
                    }
                    LOG.info("Starting Job");
                    JobStatus result = job.start(exch.getContext(), Utils.fetchUsername(exch), count);
                    LOG.info("Job " + result.getJobId() + " started");
                    String jsonResult = JsonHandler.getInstance().objectToJson(result);
                    exch.getIn().setBody(jsonResult);
                })
                .inOnly("seda:notifyJobStatusUpdate")
                .endRest()
                //
                // update
                .post("/{id}").description("Update the status of a job")
                .bindingMode(RestBindingMode.off).produces("application/json")
                .outType(JobStatus.class)
                .route()
                .log("Updating status of job ${header.id} to status ${header.Status} and processed count of ${header.ProcessedCount}")
                .process((Exchange exch) -> {
                    String id = exch.getIn().getHeader("id", String.class);
                    String status = exch.getIn().getHeader(HEADER_JOB_STATUS, String.class);
                    Integer processedCount = exch.getIn().getHeader(HEADER_JOB_PROCESSED_COUNT, Integer.class);
                    Integer errorCount = exch.getIn().getHeader(HEADER_JOB_ERROR_COUNT, Integer.class);
                    JobStatus result;
                    if (status == null) {
                        result = jobstatusClient.incrementCounts(id, processedCount, errorCount);
                        exch.getIn().setBody(result);
                    } else {
                        String event = exch.getIn().getBody(String.class);
                        result = jobstatusClient.updateStatus(id, JobStatus.Status.valueOf(status), event, processedCount, errorCount);
                        exch.getIn().setBody(result);
                    }
                    exch.getIn().setHeader(HEADER_SQUONK_USERNAME, result.getUsername());
                    String jsonResult = JsonHandler.getInstance().objectToJson(result);
                    exch.getIn().setBody(jsonResult);
                })
                .inOnly("seda:notifyJobStatusUpdate")
                .endRest();

        rest("/v1/notebooks").description("Notebook services")
                //
                // GET
                .get("/").description("Get all notebooks for the user")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookDescriptor.class)
                .param().name("user").type(query).description("The username").dataType("string").endParam()
                .route()
                .process((Exchange exch) -> {
                    String username = exch.getIn().getHeader("username", String.class);
                    List<NotebookDescriptor> results = notebookClient.listNotebooks(username);
                    exch.getIn().setBody(results);
                })
                .endRest()
                .get("/{notebookid}/e").description("Get all editables for a notebook for the user")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookEditable.class)
                .param().name("notebookid").type(path).description("Notebook ID").dataType("long").endParam()
                .param().name("user").type(query).description("The username").dataType("string").endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    String username = exch.getIn().getHeader("user", String.class);
                    List<NotebookEditable> results = notebookClient.listEditables(notebookid, username);
                    exch.getIn().setBody(results);
                })
                .endRest()
                // List<NotebookSavepoint> listSavepoints(Long notebookId);
                .get("/{notebookid}/s").description("Get all savepoints for a notebook")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookSavepoint.class)
                .param().name("notebookid").type(path).description("Notebook ID").dataType("long").endParam()
                .route()
                .process((Exchange exch) -> {
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    List<NotebookSavepoint> results = notebookClient.listSavepoints(notebookid);
                    exch.getIn().setBody(results);
                })
                .endRest()
                //NotebookDescriptor createNotebook(String username, String notebookName, String notebookDescription)
                .post("/").description("Create a new notebook")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookDescriptor.class)
                .param().name("user").type(query).description("The owner of the new notebook").dataType("string").endParam()
                .param().name("name").type(query).description("The name for the new notebook").dataType("string").endParam()
                .param().name("description").type(query).description("A description for the new notebook").dataType("string").endParam()
                .route()
                .process((Exchange exch) -> {
                    String user = exch.getIn().getHeader("user", String.class);
                    String name = exch.getIn().getHeader("name", String.class);
                    String description = exch.getIn().getHeader("description", String.class);
                    NotebookDescriptor result = notebookClient.createNotebook(user, name, description);
                    exch.getIn().setBody(result);
                })
                .endRest()
                // NotebookEditable createEditable(Long notebookId, Long parentId, String username);
                .post("/{notebookid}/e").description("Create a new editable for a notebook")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookEditable.class)
                .param().name("notebookid").type(path).description("Notebook ID").dataType("long").endParam()
                .param().name("user").type(query).description("The owner of the new notebook").dataType("string").endParam()
                .param().name("parent").type(query).description("The parent savepoint").dataType("long").endParam()
                .route()
                .process((Exchange exch) -> {
                    String user = exch.getIn().getHeader("user", String.class);
                    Long parent = exch.getIn().getHeader("parent", Long.class);
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    String description = exch.getIn().getHeader("description", String.class);
                    NotebookEditable result = notebookClient.createEditable(notebookid, parent, user);
                    exch.getIn().setBody(result);
                })
                .endRest()
                // NotebookEditable updateEditable(Long notebookId, Long editableId, String json);
                .put("/{notebookid}/e/{editableid}").description("Update the definition of an editable")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .type(NotebookInstance.class)
                .outType(NotebookEditable.class)
                .param().name("notebookid").type(path).description("Notebook ID").dataType("long").endParam()
                .param().name("editableid").type(path).description("Editable ID").dataType("long").endParam()
                .param().name("json").type(body).description("Content (as JSON)").dataType("string").endParam()
                .route()
                .process((Exchange exch) -> {
                    NotebookInstance notebookInstance = exch.getIn().getBody(NotebookInstance.class);
                    Long editableid = exch.getIn().getHeader("editableid", Long.class);
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    NotebookEditable result = notebookClient.updateEditable(notebookid, editableid, notebookInstance);
                    exch.getIn().setBody(result);
                })
                .endRest()
                // public NotebookEditable createSavepoint(Long notebookId, Long editableId);
                .post("/{notebookid}/s").description("Create a new editable for a notebook")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookEditable.class)
                .param().name("notebookid").type(path).description("Notebook ID").dataType("long").endParam()
                .param().name("editableid").type(query).description("The editable ID to make the savepoint from").dataType("long").endParam()
                .route()
                .process((Exchange exch) -> {
                    Long parent = exch.getIn().getHeader("parent", Long.class);
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    Long editableid = exch.getIn().getHeader("editableid", Long.class);
                    NotebookEditable result = notebookClient.createSavepoint(notebookid, editableid);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //NotebookSavepoint setSavepointDescription(Long notebookId, Long savepointId, String description)
                .put("/{notebookid}/s/{savepointid}/description").description("Update the description of a savepoint")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookSavepoint.class)
                .param().name("notebookid").type(path).description("Notebook ID").dataType("long").endParam()
                .param().name("savepointid").type(path).description("Savepoint ID").dataType("long").endParam()
                .param().name("description").type(query).description("New description").dataType("string").endParam()
                .route()
                .process((Exchange exch) -> {
                    String description = exch.getIn().getHeader("description", String.class);
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    Long savepointid = exch.getIn().getHeader("savepointid", Long.class);
                    NotebookSavepoint result = notebookClient.setSavepointDescription(notebookid, savepointid, description);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //NotebookSavepoint setSavepointLabel(Long notebookId, Long savepointId, String label);
                .put("/{notebookid}/s/{savepointid}/label").description("Update the label of a savepoint")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookSavepoint.class)
                .param().name("notebookid").type(path).description("Notebook ID").dataType("long").endParam()
                .param().name("savepointid").type(path).description("Savepoint ID").dataType("long").endParam()
                .param().name("label").type(query).description("New Label").dataType("string").endParam()
                .route()
                .process((Exchange exch) -> {
                    String label = exch.getIn().getHeader("label", String.class);
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    Long savepointid = exch.getIn().getHeader("savepointid", Long.class);
                    NotebookSavepoint result = notebookClient.setSavepointLabel(notebookid, savepointid, label);
                    exch.getIn().setBody(result);
                })
                .endRest()
                // public NotebookDescriptor updateNotebook(Long notebookId, String name, String description)
                .put("/{notebookid}").description("Update the label of a savepoint")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(NotebookDescriptor.class)
                .param().name("notebookid").type(path).description("Notebook ID").dataType("long").endParam()
                .param().name("name").type(query).description("New name").dataType("string").endParam()
                .param().name("description").type(query).description("New description").dataType("string").endParam()
                .route()
                .process((Exchange exch) -> {
                    String name = exch.getIn().getHeader("name", String.class);
                    String description = exch.getIn().getHeader("description", String.class);
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    NotebookDescriptor result = notebookClient.updateNotebook(notebookid, name, description);
                    exch.getIn().setBody(result);
                })
                .endRest()
                //
                // VARIABLES
                //
                // GET
                .get("/{notebookid}/v/{varname}/{type}/{key}").description("Read a variable value using either its label or its editable/savepoint id")
                .bindingMode(RestBindingMode.off)
                .param().name("notebookid").type(path).description("The notebook ID").dataType("long").required(true).endParam()
                .param().name("varname").type(path).description("The name of the variable").dataType("string").required(true).endParam()
                .param().name("key").type(path).description("Optional key for the variable. If not provide key of 'default' is assumed").dataType("string").required(false).endParam()
                .param().name("type").type(path).description("The type of variable (s = stream, t = text)").dataType("string").required(true).allowableValues("s", "t").endParam()
                .param().name("label").type(query).description("The label of the variable").dataType("string").required(false).endParam()
                .param().name("sourceid").type(query).description("The editable/savepoint ID").dataType("long").required(false).endParam()
                .route()
                .process((Exchange exch) -> {
                    String varname = exch.getIn().getHeader("varname", String.class);
                    String key = exch.getIn().getHeader("key", String.class);
                    VarType type = VarType.valueOf(exch.getIn().getHeader("type", String.class));
                    String label = exch.getIn().getHeader("label", String.class);
                    Long sourceid = exch.getIn().getHeader("sourceid", Long.class);
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);

                    if (notebookid == null) {
                        throw new IllegalArgumentException("Must specify notebookid");
                    }
                    if (varname == null) {
                        throw new IllegalArgumentException("Must specify variable name");
                    }
                    if (type == null) {
                        throw new IllegalArgumentException("Must specify variable type");
                    }
                    // TODO -set the mime type and encoding
                    if (label != null) {
                        switch (type) {
                            case s:
                                InputStream is = notebookClient.readStreamValue(notebookid, label, varname, key);
                                exch.getIn().setBody(is);
                                break;
                            case t:
                                LOG.info("reading text value for label");
                                String t = notebookClient.readTextValue(notebookid, label, varname, key);
                                exch.getIn().setBody(t);
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid variable type. Must be s (stream) or t (text)");
                        }
                    } else if (sourceid != null) {
                        switch (type) {
                            case s:
                                InputStream is = notebookClient.readStreamValue(notebookid, sourceid, varname, key);
                                exch.getIn().setBody(is);
                                break;
                            case t:
                                String t = notebookClient.readTextValue(notebookid, sourceid, varname, key);
                                exch.getIn().setBody(t);
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid variable type. Must be s (stream) or t (text)");
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid variable type. Must be s (stream) or t (text)");
                    }
                })
                .endRest()
                // write a variable
                .post("/{notebookid}/v/{varname}/{type}/{key}").description("Write a variable value")
                .bindingMode(RestBindingMode.off)
                .param().name("notebookid").type(path).description("The notebook ID").dataType("long").required(true).endParam()
                .param().name("varname").type(path).description("The name of the variable").dataType("string").required(true).endParam()
                .param().name("key").type(path).description("Optional key for the variable. If not provide key of 'default' is assumed").dataType("string").required(false).endParam()
                .param().name("type").type(path).description("The type of variable (s = stream, t = text)").dataType("string").required(true).allowableValues("s", "t").endParam()
                .param().name("editableid").type(query).description("The editable ID").dataType("long").required(true).endParam()
                .param().name("cellid").type(query).description("The cell ID that produces the value").dataType("long").required(true).endParam()
                .param().name("body").type(body).description("The value").required(true).endParam()
                .route()
                .process((Exchange exch) -> {
                    String varname = exch.getIn().getHeader("varname", String.class);
                    String key = exch.getIn().getHeader("key", String.class);
                    VarType type = VarType.valueOf(exch.getIn().getHeader("type", String.class));
                    Long notebookid = exch.getIn().getHeader("notebookid", Long.class);
                    Long editableid = exch.getIn().getHeader("editableid", Long.class);
                    Long cellid = exch.getIn().getHeader("cellid", Long.class);


                    if (notebookid == null) {
                        throw new IllegalArgumentException("Must specify notebookid");
                    }
                    if (editableid == null) {
                        throw new IllegalArgumentException("Must specify editableid");
                    }
                    if (cellid == null) {
                        throw new IllegalArgumentException("Must specify cellid");
                    }
                    if (varname == null) {
                        throw new IllegalArgumentException("Must specify variable name");
                    }
                    if (type == null) {
                        throw new IllegalArgumentException("Must specify variable type");
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
                .endRest();

        from("seda:notifyJobStatusUpdate")
                .setHeader("rabbitmq.ROUTING_KEY", simple("users.${header[" + HEADER_SQUONK_USERNAME + "]}.jobstatus"))
                //.log("Routing Key: ${header[rabbitmq.ROUTING_KEY]}")
                .inOnly(userNotifyMqueueUrl);


        rest("/v1/users").description("User management services")
                //
                // GET statuses
                .get("/{" + HEADER_SQUONK_USERNAME + "}").description("Get the User object for this username (spceified as the query parameter named " + HEADER_SQUONK_USERNAME)
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .outType(User.class)
                .route()
                .process((Exchange exch) -> UserHandler.putUser(exch))
                .endRest();



        /* These are the implementation endpoints - not accessible directly from "outside"
         */
        from("direct:datasets/upload")
                .process((Exchange exchange) -> {
                            String specifiedName = exchange.getIn().getHeader(HEADER_DATAITEM_NAME, String.class);
                            String username = Utils.fetchUsername(exchange);
                            DataItem created = null;
                            InputStream body = exchange.getIn().getBody(InputStream.class);
                            String contentType = exchange.getIn().getHeader("Content-Type", String.class);
                            if (body != null) {
                                DatasetHandler datasetHandler = Utils.getDatasetHandler(exchange);
                                InputStream gunzip = IOUtils.getGunzippedInputStream(body);
                                Stream<MoleculeObject> mols = null;
                                try {
                                    if (contentType != null && "chemical/x-mdl-sdfile".equals(contentType)) {
                                        mols = MoleculeObjectUtils.createStreamGenerator(gunzip).getStream(false);
                                    } else { // assume MoleculeObject JSON
                                        // need to convert to objects so that the metadata can be generated
                                        mols = (Stream<MoleculeObject>) datasetHandler.generateObjectFromJson(body, new Metadata(MoleculeObject.class.getName(), Metadata.Type.STREAM, 0));
                                    }

                                    DataItem result = datasetHandler.createDataset(
                                            username,
                                            mols,
                                            specifiedName == null ? "File uploaded on " + new Date().toString() : specifiedName);
                                    if (result != null) {
                                        created = result;
                                    }

                                } finally {
                                    if (mols != null) {
                                        mols.close();
                                    }
                                }

                            }
                            exchange.getOut().setBody(created);
                        }
                )
                .marshal()
                .json(JsonLibrary.Jackson);

    }

}
