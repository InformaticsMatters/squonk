package org.squonk.openchemlib.services;

import com.im.lac.dataset.Metadata;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;
import org.squonk.types.TypeResolver;

import javax.inject.Inject;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_BASIC_JSON;
import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON;

/**
 * @author timbo
 */
public class OpenChemLibRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(OpenChemLibRestRouteBuilder.class.getName());

    @Inject
    private TypeResolver resolver;

    private static final String ROUTE_VERIFY = "verify";
    private static final String ROUTE_LOGP = "logp";
    private static final String ROUTE_LOGS = "logs";
    private static final String ROUTE_PSA = "psa";

    private static final String ROUTE_STATS = "seda:post_stats";


    protected static final ServiceDescriptor[] CALCULATORS_SERVICE_DESCRIPTOR
            = new ServiceDescriptor[]{
            createServiceDescriptor(
                    "ocl.calculators.verify",
                    "Verify structure (OCL)",
                    "Verify that the molecules are valid according to OpenChemLib",
                    new String[]{"verify", "openchemlib"},
                    new String[]{"/Chemistry/Toolkits/RDKit/Verify", "/Chemistry/Verify"},
                    "icons/properties_add.png",
                    ROUTE_VERIFY,
                    new OptionDescriptor[] {OptionDescriptor.IS_FILTER, OptionDescriptor.FILTER_MODE}),
            createServiceDescriptor(
                    "ocl.logp", "LogP (OpenChemLib)", "OpenChemLib LogP prediction",
                    new String[]{"logp", "partitioning", "molecularproperties", "openchemlib"},
                    new String[]{"/Chemistry/Toolkits/OpenChemLib/Calculators", "/Chemistry/Calculators/Partioning"},
                    "icons/properties_add.png", ROUTE_LOGP, null),
            createServiceDescriptor(
                    "ocl.logs", "LogS (OpenChemLib)", "OpenChemLib Aqueous Solubility prediction",
                    new String[]{"logp", "solubility", "molecularproperties", "openchemlib"},
                    new String[]{"/Chemistry/Toolkits/OpenChemLib/Calculators", "/Chemistry/Calculators/Solubility"},
                    "icons/properties_add.png", ROUTE_LOGS, null),
            createServiceDescriptor(
                    "ocl.psa", "PSA (OpenChemLib)", "OpenChemLib Polar Surface Area prediction",
                    new String[]{"logp", "psa", "molecularproperties", "openchemlib"},
                    new String[]{"/Chemistry/Toolkits/OpenChemLib/Calculators", "/Chemistry/Calculators/Other"},
                    "icons/properties_add.png", ROUTE_PSA, null)
    };


    private static ServiceDescriptor createServiceDescriptor(String id, String name, String description, String[] tags, String[] paths, String icon, String endpoint, OptionDescriptor[] options) {
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
                                null, null, null, null, options, StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                }
        );
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0")
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "OpenChemLib Basic services").apiProperty("api.version", "1.0")
                .apiProperty("cors", "true");

        from(ROUTE_STATS)
                .log("Posting stats for ${header.SquonkJobID} ${body}");

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
                //
                // service descriptor
                .get().description("ServiceDescriptors for OpenChemLib calculators")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(CALCULATORS_SERVICE_DESCRIPTOR);
                })
                .endRest()
                //
                .post(ROUTE_VERIFY).description("Verify as OpenChemLib molecules")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_STRUCTURE_VERIFY, resolver, ROUTE_STATS))
                .endRest()
                //
                .post(ROUTE_LOGP).description("Calculate the LogP of the supplied structures")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_LOGP, resolver, ROUTE_STATS))
                .endRest()
                //
                .post(ROUTE_LOGS).description("Calculate the aqueous solubility of the supplied structures")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_LOGS, resolver, ROUTE_STATS))
                .endRest()
                //
                .post(ROUTE_PSA).description("Calculate the polar surface area of the supplied structures")
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
