package org.squonk.cdk.services;

import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import org.squonk.camel.processor.MoleculeObjectDatasetHttpProcessor;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.util.CamelUtils;
import org.squonk.types.CDKSDFile;
import org.squonk.types.TypeResolver;
import static org.squonk.types.TypeResolver.*;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class CdkRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(CdkRestRouteBuilder.class.getName());

    private static final TypeResolver resolver = new TypeResolver();

    protected static final ServiceDescriptor[] CALCULATORS_SERVICE_DESCRIPTOR
            = new ServiceDescriptor[]{new ServiceDescriptor(
                        "cdk.calculators",
                        "CDK LogP",
                        "CDK LogP predictions for XLogP and ALogP",
                        new String[]{"logp", "partitioning", "molecularproperties", "cdk"},
                        null,
                        new String[]{"/Chemistry/Toolkits/CDK/Calculators", "Chemistry/Calculators/Partioning"},
                        "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
                        null,
                        new String[]{"public"},
                        MoleculeObject.class, // inputClass
                        MoleculeObject.class, // outputClass
                        Metadata.Type.STREAM, // inputTypes
                        Metadata.Type.STREAM, // outputTypes
                        "icons/properties_add.png",
                        new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "logp", // endpoint
                                    true, // URL is relative
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null)
                        }
                )
            };

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
                .post("logp").description("Calculate the logP for the supplied MoleculeObjects")
                .consumes(MIME_TYPE_DATASET_MOLECULE_JSON)
                .produces(MIME_TYPE_DATASET_MOLECULE_JSON)
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(CdkCalculatorsRouteBuilder.CDK_LOGP)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                // experimental flexible services
                .post("logp2").description("Calculate the logP for the supplied MoleculeObjects")
                .consumes(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_MDL_SDF))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_BASIC_OBJECT_JSON, MIME_TYPE_MDL_SDF))
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(CdkCalculatorsRouteBuilder.CDK_LOGP + "_2", resolver, CDKSDFile.class))
                .endRest();

    }

    String join(String... args) {
        return Stream.of(args).collect(Collectors.joining(","));
    }

}
