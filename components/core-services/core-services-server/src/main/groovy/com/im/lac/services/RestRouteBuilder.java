package com.im.lac.services;

import com.im.lac.services.util.Utils;
import com.im.lac.dataset.DataItem;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.IOUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.discovery.service.ServiceDiscoveryRouteBuilder;
import com.im.lac.services.job.service.JobHandler;
import com.im.lac.services.job.service.JobServiceRouteBuilder;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;

/**
 *
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder implements ServerConstants {

    private static final Logger LOG = Logger.getLogger(RestRouteBuilder.class.getName());

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
                .consumes("application/json")
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    String s = exch.getIn().getBody(String.class);
                    exch.getIn().setBody(s);
                })
                .log("Echoing: ${body}")
                .endRest();

        rest("/v1/services")
                .get().description("Get service definitions for the available services")
                .bindingMode(RestBindingMode.json)
                .outType(ServiceDescriptorSet.class)
                .produces("application/json")
                .to(ServiceDiscoveryRouteBuilder.ROUTE_REQUEST);

        rest("/v1/datasets").description("Dataset management services")
                // POST
                .post()
                .description("Upload file to create new dataset. File is the body and dataset name is given by the header named " + DataItem.HEADER_DATA_ITEM_NAME)
                .bindingMode(RestBindingMode.off)
                .produces("application/json")
                .to("direct:datasets/upload")
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
//                //
//                // POST new split and queue job
//                .post("/splitAndQueueProcessDataset").description("Submit a new split and queue job")
//                .bindingMode(RestBindingMode.json)
//                .consumes("application/json").type(SplitAndQueueProcessDatasetJobDefinition.class)
//                .produces("application/json").outType(JobStatus.class)
//                .to(JobServiceRouteBuilder.ROUTE_SUBMIT_JOB)
//                //
//                // POST new DoNothingJobDefintion
//                .post("/doNothingJob").description("Noop job for testing. Does nothing but returns a status")
//                .bindingMode(RestBindingMode.json)
//                .consumes("application/json").type(DoNothingJobDefinition.class)
//                .produces("application/json").outType(JobStatus.class)
//                .to(JobServiceRouteBuilder.ROUTE_DO_NOTHING);


        /* These are the implementation endpoints - not accessible directly from "outside"
         */
        from("direct:datasets/upload")
                .process((Exchange exchange) -> {
                    String specifiedName = exchange.getIn().getHeader(DataItem.HEADER_DATA_ITEM_NAME, String.class);
                    DataItem created = null;
                    InputStream body = exchange.getIn().getBody(InputStream.class);
                    if (body != null) {
                        DatasetHandler datasetHandler = Utils.getDatasetHandler(exchange);
                        InputStream gunzip = IOUtils.getGunzippedInputStream(body);
                        // TODO - allow this to handle things other than molecules.
                        try (Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(gunzip).getStream(false)) {
                            DataItem result = datasetHandler.createDataset(
                                    mols,
                                    specifiedName == null ? "File uploaded on " + new Date().toString() : specifiedName);
                            if (result != null) {
                                created = result;
                            }
                        }

                    }
                    exchange.getOut().setBody(created);
                })
                .marshal().json(JsonLibrary.Jackson);

    }

}
