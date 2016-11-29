package org.squonk.openchemlib.services;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.types.TypeResolver;

import javax.inject.Inject;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_BASIC_JSON;
import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;

/**
 * @author timbo
 */
public class OpenChemLibRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(OpenChemLibRestRouteBuilder.class.getName());

    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.ocl";

    @Inject
    private TypeResolver resolver;

    private static final String ROUTE_STATS = "seda:post_stats";

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0")
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "OpenChemLib Basic services").apiProperty("api.version", "1.0")
                .apiProperty("cors", "true");

        // send usage metrics to the message queue
        from(ROUTE_STATS)
                .marshal().json(JsonLibrary.Jackson)
                .to(mqueueUrl);

        //These are the REST endpoints - exposed as public web services
        //
        // test like this:
        // curl "http://localhost:8080/chem-services-openchemlib-basic/rest/ping"
        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();


        // test like this:
        // curl -X POST -T mols.json "http://localhost:8080/chem-services-openchemlib-basic/rest/v1/calculators/logp"
        rest("/v1/calculators").description("Property calculation services using OpenChemLib")
                .bindingMode(RestBindingMode.off)
                // service descriptor
                .get().description("ServiceDescriptors for OpenChemLib calculators")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(OpenChemLibBasicServices.ALL);
                })
                .endRest()
                //
                .post(OpenChemLibBasicServices.SERVICE_DESCRIPTOR_VERIFY.getExecutionEndpoint())
                .description(OpenChemLibBasicServices.SERVICE_DESCRIPTOR_VERIFY.getDescription())
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_STRUCTURE_VERIFY, resolver, ROUTE_STATS))
                .endRest()
                //
                .post(OpenChemLibBasicServices.SERVICE_DESCRIPTOR_LOGP.getExecutionEndpoint())
                .description(OpenChemLibBasicServices.SERVICE_DESCRIPTOR_LOGP.getDescription())
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_LOGP, resolver, ROUTE_STATS))
                .endRest()
                //
                .post(OpenChemLibBasicServices.SERVICE_DESCRIPTOR_LOGS.getExecutionEndpoint())
                .description(OpenChemLibBasicServices.SERVICE_DESCRIPTOR_LOGS.getDescription())
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_LOGS, resolver, ROUTE_STATS))
                .endRest()
                //
                .post(OpenChemLibBasicServices.SERVICE_DESCRIPTOR_PSA.getExecutionEndpoint())
                .description(OpenChemLibBasicServices.SERVICE_DESCRIPTOR_PSA.getDescription())
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_PSA, resolver, ROUTE_STATS))
                .endRest();

    }

    String join(String... args) {
        return Stream.of(args).collect(Collectors.joining(","));
    }

}
