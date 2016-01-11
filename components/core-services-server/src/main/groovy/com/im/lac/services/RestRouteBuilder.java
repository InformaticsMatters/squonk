package com.im.lac.services;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.discovery.service.ServiceDiscoveryRouteBuilder;
import com.im.lac.services.job.dao.MemoryJobStatusClient;
import com.im.lac.services.job.service.JobHandler;
import com.im.lac.services.job.service.JobServiceRouteBuilder;
import com.im.lac.services.user.User;
import com.im.lac.services.user.UserHandler;
import com.im.lac.services.util.Utils;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.chemaxon.molecule.MoleculeObjectUtils;
import org.squonk.client.JobStatusClient;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_USERS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_USERS_EXCHANGE_PARAMS;

/**
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder implements ServerConstants {

    private static final Logger LOG = Logger.getLogger(RestRouteBuilder.class.getName());

    private final JobStatusClient jobstatusClient = MemoryJobStatusClient.INSTANCE;
    private final JsonHandler jsonHandler = JsonHandler.getInstance();
    private MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials();
    private final String userNotifyMqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_USERS_EXCHANGE_NAME, MQUEUE_USERS_EXCHANGE_PARAMS);

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("/ping")
                .get().description("Simple ping service to check things are running")
                .produces("text/plain")
                .route()
                .transform(constant("Ping\n")).endRest();

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
                .description("Upload file to create new dataset. File is the body and dataset name is given by the header named " + CommonConstants.HEADER_DATAITEM_NAME)
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

        rest("/v1/jobs").description("Job submission and management services")
                //
                // GET statuses
                // TODO - handle filter criteria
                .get("/").description("Get the statuses of jobs")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> JobHandler.putJobStatuses(exch))
                .endRest()
                //
                // GET status
                .get("/{" + REST_JOB_ID + "}").description("Get the latest status of an individual job")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> JobHandler.putUpdatedJobStatus(exch))
                .endRest()
                //
                // POST new async process dataset job
                // can be testing by posing JSON like this: 
                // {"endpoint": "direct:simpleroute", "datasetId": 44, "mode": "CREATE", "datasetName": "holy cow","resultType": "com.im.lac.types.MoleculeObject"}
                .post().description("Submit a new job defined the by the supplied JobDefinition")
                .bindingMode(RestBindingMode.json)
                .consumes("application/json").type(JobDefinition.class)
                .produces("application/json").outType(JobStatus.class)
                .route()
                .log("REST POST jobdef: ${body}")
                .to(JobServiceRouteBuilder.ROUTE_SUBMIT_JOB)
                .endRest();


        rest("/v1/jobstatus").description("JobStatus services")
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
                // create new job status
                .post("/").description("Create a new job status")
                .bindingMode(RestBindingMode.off).produces("application/json")
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    String json = exch.getIn().getBody(String.class);
                    LOG.finer("JSON is " + json);
                    JobDefinition jobdef = JsonHandler.getInstance().objectFromJson(json, JobDefinition.class);
                    LOG.fine("Received request to create job for " + jobdef);
                    String username = exch.getIn().getHeader(HEADER_SQUONK_USERNAME, String.class);
                    if (username == null) {
                        throw new IllegalStateException("Username not specified");
                    }
                    JobStatus result = jobstatusClient.create(jobdef, username, 0);
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
                    String status = exch.getIn().getHeader("Status", String.class);
                    Integer processedCount = exch.getIn().getHeader("ProcessedCount", Integer.class);
                    JobStatus result;
                    if (status == null) {
                        result = jobstatusClient.incrementProcesssedCount(id, processedCount);
                        exch.getIn().setBody(result);
                    } else {
                        String event = exch.getIn().getBody(String.class);
                        result = jobstatusClient.updateStatus(id, JobStatus.Status.valueOf(status), event, processedCount);
                        exch.getIn().setBody(result);
                    }
                    exch.getIn().setHeader(CommonConstants.HEADER_SQUONK_USERNAME, result.getUsername());
                    String jsonResult = JsonHandler.getInstance().objectToJson(result);
                    exch.getIn().setBody(jsonResult);
                })
                .inOnly("seda:notifyJobStatusUpdate")
                .endRest();

        from("seda:notifyJobStatusUpdate")
                .setHeader("rabbitmq.ROUTING_KEY", simple("users.${header[" + HEADER_SQUONK_USERNAME + "]}.jobstatus"))
                //.log("Routing Key: ${header[rabbitmq.ROUTING_KEY]}")
                .inOnly(userNotifyMqueueUrl);


        rest("/v1/users").description("User management services")
                //
                // GET statuses
                .get("/{" + CommonConstants.HEADER_SQUONK_USERNAME + "}").description("Get the User object for this username (spceified as the query parameter named " + CommonConstants.HEADER_SQUONK_USERNAME)
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
                            String specifiedName = exchange.getIn().getHeader(CommonConstants.HEADER_DATAITEM_NAME, String.class);
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
                                        mols = (Stream<MoleculeObject>) datasetHandler.generateObjectFromJson(body, new Metadata(MoleculeObject.class.getName(), Metadata.Type.ARRAY, 0));
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
