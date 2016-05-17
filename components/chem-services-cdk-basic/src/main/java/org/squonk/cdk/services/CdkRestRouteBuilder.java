package org.squonk.cdk.services;

import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.cdk.processor.CDKMoleculeObjectSDFileProcessor;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.CDKSDFile;
import org.squonk.types.TypeResolver;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author timbo
 */
public class CdkRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(CdkRestRouteBuilder.class.getName());

    private static final TypeResolver resolver = new TypeResolver();

    private static final String ROUTE_LOGP = "logp";
    private static final String ROUTE_DONORS_ACCEPTORS = "donors_acceptors";
    private static final String ROUTE_WIENER_NUMBERS = "wiener_numbers";

    private static final String ROUTE_CONVERT_TO_SDF = "convert_to_sdf";

    private static final String ROUTE_STATS = "seda:post_stats";



    protected static final ServiceDescriptor[] CALCULATORS_SERVICE_DESCRIPTOR
            = new ServiceDescriptor[]{
            createServiceDescriptor(
                    "cdk.logp", "LogP (CDK)", "LogP predictions for XLogP, ALogP and AMR using CDK",
                    new String[]{"logp", "partitioning", "molecularproperties", "cdk"},
                    new String[]{"/Chemistry/Toolkits/CDK/Calculators", "/Chemistry/Calculators/Partioning"},
                    "icons/properties_add.png", ROUTE_LOGP),
            createServiceDescriptor(
                    "cdk.donors_acceptors", "HBA & HBD (CDK)", "H-bond donor and acceptor counts using CDK",
                    new String[]{"hbd", "donors", "hba", "acceptors", "topology", "molecularproperties", "cdk"},
                    new String[]{"/Chemistry/Toolkits/CDK/Calculators", "/Chemistry/Calculators/Topological"},
                    "icons/properties_add.png", ROUTE_DONORS_ACCEPTORS),
            createServiceDescriptor(
                    "cdk.wiener_numbers", "Wiener Numbers (CDK)", "Wiener path and polarity numbers using CDK",
                    new String[]{"wiener", "topology", "molecularproperties", "cdk"},
                    new String[]{"/Chemistry/Toolkits/CDK/Calculators", "/Chemistry/Calculators/Topological"},
                    "icons/properties_add.png", ROUTE_WIENER_NUMBERS)
    };

    protected static final ServiceDescriptor[] CONVERTERS_SERVICE_DESCRIPTOR
            = new ServiceDescriptor[]{
            createServiceDescriptor(
                    "cdk.export.sdf", "SDF Export (CDK)", "Convert to SD file format using CDK",
                    new String[]{"export", "sdf", "sdfile", "cdk"},
                    new String[]{"/Chemistry/Toolkits/CDK/IO"},
                    "default_icon.png", ROUTE_CONVERT_TO_SDF)
    };

    private static ServiceDescriptor createServiceDescriptor(String id, String name, String description, String[] tags, String[] paths, String icon, String endpoint) {
        return new ServiceDescriptor(
                id, name, description, tags, null, paths,
                "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
                null,
                new String[]{"public"},
                MoleculeObject.class, // inputClass
                MoleculeObject.class, // outputClass
                Metadata.Type.STREAM, // inputTypes
                Metadata.Type.STREAM, // outputTypes
                icon,
                new AccessMode[]{
                        new AccessMode(
                                "asyncHttp",
                                "Immediate execution",
                                "Execute as an asynchronous REST web service",
                                endpoint, // endpoint
                                true, // URL is relative
                                AsyncHttpProcessDatasetJobDefinition.class,
                                null, null, null, null, null,
                                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                }
        );
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");
//                .apiContextPath("/api-doc")
//                .apiProperty("api.title", "CDK Basic services").apiProperty("api.version", "1.0")
//                .apiProperty("cors", "true");

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
                //
                // service descriptor
                .get().description("ServiceDescriptors for CDK calculators")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(CALCULATORS_SERVICE_DESCRIPTOR);
                })
                .endRest()
                //
                .post(ROUTE_LOGP).description("Calculate the logP for the supplied structures")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MoleculeObjectRouteHttpProcessor.DEFAULT_OUTPUT_MIME_TYPES))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(CdkCalculatorsRouteBuilder.CDK_LOGP, resolver, ROUTE_STATS, CDKSDFile.class))
                .endRest()
                //
                .post(ROUTE_DONORS_ACCEPTORS).description("Calculate hydrogen bond donor and acceptor counts for the supplied structures")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MoleculeObjectRouteHttpProcessor.DEFAULT_OUTPUT_MIME_TYPES))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(CdkCalculatorsRouteBuilder.CDK_DONORS_ACCEPTORS, resolver, ROUTE_STATS, CDKSDFile.class))
                .endRest()
                //
                .post(ROUTE_WIENER_NUMBERS).description("Calculate Wiener path and polarity for the supplied structures")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MoleculeObjectRouteHttpProcessor.DEFAULT_OUTPUT_MIME_TYPES))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(CdkCalculatorsRouteBuilder.CDK_WIENER_NUMBERS, resolver, ROUTE_STATS, CDKSDFile.class))
                .endRest();


        rest("/v1/converters").description("Molecule format conversion services using CDK")
                .bindingMode(RestBindingMode.off)
                //
                // service descriptor
                .get().description("ServiceDescriptors for CDK format conversions")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(CONVERTERS_SERVICE_DESCRIPTOR);
                })
                .endRest()
                // Convert to SDF
                .post(ROUTE_CONVERT_TO_SDF).description("Convert MoleculeObjects to SD file format using CDK")
                .consumes(join(CDKMoleculeObjectSDFileProcessor.INPUT_MIME_TYPES))
                .produces(join(CDKMoleculeObjectSDFileProcessor.OUTPUT_MIME_TYPES))
                .route()
                .process(new CDKMoleculeObjectSDFileProcessor(resolver))
                .endRest();

        from(ROUTE_STATS)
                .log("Posting stats for ${header.SquonkJobID}");

    }

    String join(String... args) {
        return Stream.of(args).collect(Collectors.joining(","));
    }

}
