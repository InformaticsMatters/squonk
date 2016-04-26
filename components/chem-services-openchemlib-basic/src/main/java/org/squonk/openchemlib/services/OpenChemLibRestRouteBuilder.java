package org.squonk.openchemlib.services;

import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
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
@ContextName("openchemlib")
public class OpenChemLibRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(OpenChemLibRestRouteBuilder.class.getName());

    @Inject
    private TypeResolver resolver;

    private static final String ROUTE_LOGP = "logp";
    private static final String ROUTE_LOGS = "logs";
    private static final String ROUTE_PSA = "psa";


    protected static final ServiceDescriptor[] CALCULATORS_SERVICE_DESCRIPTOR
            = new ServiceDescriptor[]{
            createServiceDescriptor(
                    "ocl.logp", "LogP (OpenChemLib)", "OpenChemLib LogP prediction",
                    new String[]{"logp", "partitioning", "molecularproperties", "openchemlib"},
                    new String[]{"/Chemistry/Toolkits/OpenChemLib/Calculators", "/Chemistry/Calculators/Partioning"},
                    "icons/properties_add.png", ROUTE_LOGP),
            createServiceDescriptor(
                    "ocl.logs", "LogS (OpenChemLib)", "OpenChemLib Aqueous Solubility prediction",
                    new String[]{"logp", "solubility", "molecularproperties", "openchemlib"},
                    new String[]{"/Chemistry/Toolkits/OpenChemLib/Calculators", "/Chemistry/Calculators/Solubility"},
                    "icons/properties_add.png", ROUTE_LOGS),
            createServiceDescriptor(
                    "ocl.psa", "PSA (OpenChemLib)", "OpenChemLib Polar Surface Area prediction",
                    new String[]{"logp", "psa", "molecularproperties", "openchemlib"},
                    new String[]{"/Chemistry/Toolkits/OpenChemLib/Calculators", "/Chemistry/Calculators/Other"},
                    "icons/properties_add.png", ROUTE_PSA)

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
                                null, null, null, null, null, StepDefinitionConstants.ServiceExecutor.CLASSNAME)
                }
        );
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0")
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "OpenChemLib Basic services").apiProperty("api.version", "1.0")
                .apiProperty("cors", "true")
        ;

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
                .post(ROUTE_LOGP).description("Calculate the LogP of the supplied structures")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_LOGP, resolver))
                .endRest()
                //
                .post(ROUTE_LOGS).description("Calculate the aqueous solubility of the supplied structures")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_LOGS, resolver))
                .endRest()
                //
                .post(ROUTE_PSA).description("Calculate the polar surface area of the supplied structures")
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(OpenChemLibCalculatorsRouteBuilder.OCL_PSA, resolver))
                .endRest()

        ;

    }

    String join(String... args) {
        return Stream.of(args).collect(Collectors.joining(","));
    }

}
