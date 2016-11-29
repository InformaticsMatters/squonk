package org.squonk.cdk.services;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.cdk.processor.CDKMoleculeObjectSDFileProcessor;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.types.CDKSDFile;
import org.squonk.types.TypeResolver;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;

/**
 * @author timbo
 */
public class CdkRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(CdkRestRouteBuilder.class.getName());

    private static final TypeResolver resolver = new TypeResolver();

    private static final String ROUTE_CONVERT_TO_SDF = "convert_to_sdf";

    private static final String ROUTE_STATS = "seda:post_stats";

    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.cdk";

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");
//                .apiContextPath("/api-doc")
//                .apiProperty("api.title", "CDK Basic services").apiProperty("api.version", "1.0")
//                .apiProperty("cors", "true");

        // send usage metrics to the message queue
        from(ROUTE_STATS)
                .marshal().json(JsonLibrary.Jackson)
                .to(mqueueUrl);

        //These are the REST endpoints - exposed as public web services
        //
        // test like this:
        // curl "http://localhost:8080/chem-services-cdk-basic/rest/ping"
        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();


        // test like this:
        // curl -X POST -T mols.json "http://localhost:8080/chem-services-cdk-basic/rest/v1/calculators/logp"
        rest("/v1/calculators").description("Property calculation services using CDK")
                .bindingMode(RestBindingMode.off)
                // service descriptor
                .get().description("ServiceDescriptors for CDK calculators")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(CdkBasicServices.ALL);
                })
                .endRest()
                //
                .post(CdkBasicServices.SERVICE_DESCRIPTOR_VERIFY.getExecutionEndpoint())
                .description(CdkBasicServices.SERVICE_DESCRIPTOR_VERIFY.getDescription())
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(CdkCalculatorsRouteBuilder.CDK_STRUCTURE_VERIFY, resolver, ROUTE_STATS))
                .endRest()
                //
                .post(CdkBasicServices.SERVICE_DESCRIPTOR_LOGP.getExecutionEndpoint())
                .description(CdkBasicServices.SERVICE_DESCRIPTOR_LOGP.getDescription())
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MoleculeObjectRouteHttpProcessor.DEFAULT_OUTPUT_MIME_TYPES))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(CdkCalculatorsRouteBuilder.CDK_LOGP, resolver, ROUTE_STATS, CDKSDFile.class))
                .endRest()
                //
                .post(CdkBasicServices.SERVICE_DESCRIPTOR_HBA_HBD.getExecutionEndpoint())
                .description(CdkBasicServices.SERVICE_DESCRIPTOR_HBA_HBD.getDescription())
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MoleculeObjectRouteHttpProcessor.DEFAULT_OUTPUT_MIME_TYPES))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(CdkCalculatorsRouteBuilder.CDK_DONORS_ACCEPTORS, resolver, ROUTE_STATS, CDKSDFile.class))
                .endRest()
                //
                .post(CdkBasicServices.SERVICE_DESCRIPTOR_WIENER_NUMBERS.getExecutionEndpoint())
                .description(CdkBasicServices.SERVICE_DESCRIPTOR_WIENER_NUMBERS.getDescription())
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MoleculeObjectRouteHttpProcessor.DEFAULT_OUTPUT_MIME_TYPES))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(CdkCalculatorsRouteBuilder.CDK_WIENER_NUMBERS, resolver, ROUTE_STATS, CDKSDFile.class))
                .endRest();


        rest("/v1/converters").description("Molecule format conversion services using CDK")
                .bindingMode(RestBindingMode.off)
                // service descriptor
                .get().description("ServiceDescriptors for CDK convertors")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(CdkConverterServices.ALL);
                })
                .endRest()
                //
                .post(CdkConverterServices.SERVICE_DESCRIPTOR_CONVERT_TO_SDF.getExecutionEndpoint())
                .description(CdkConverterServices.SERVICE_DESCRIPTOR_CONVERT_TO_SDF.getDescription())
                .consumes(join(CDKMoleculeObjectSDFileProcessor.INPUT_MIME_TYPES))
                .produces(join(CDKMoleculeObjectSDFileProcessor.OUTPUT_MIME_TYPES))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(null, resolver, ROUTE_STATS, CDKSDFile.class))
                .endRest();

    }

    String join(String... args) {
        return Stream.of(args).collect(Collectors.joining(","));
    }

}
