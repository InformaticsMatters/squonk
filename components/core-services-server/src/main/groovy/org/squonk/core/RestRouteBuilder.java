package org.squonk.core;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.core.dataset.service.DatasetHandler;
import org.squonk.core.discovery.service.ServiceDiscoveryRouteBuilder;
import org.squonk.core.job.Job;
import org.squonk.core.job.service.MemoryJobStatusClient;
import org.squonk.core.job.service.StepsCellJob;
import org.squonk.core.user.User;
import org.squonk.core.user.UserHandler;
import org.squonk.core.util.Utils;
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
                        job = new StepsCellJob((ExecuteCellUsingStepsJobDefinition)jobdef);
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
