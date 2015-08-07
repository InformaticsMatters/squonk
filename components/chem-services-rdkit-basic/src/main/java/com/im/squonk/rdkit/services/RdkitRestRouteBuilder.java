package com.im.squonk.rdkit.services;

import com.im.lac.camel.util.CamelUtils;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.services.AccessMode;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.types.MoleculeObject;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 *
 * @author timbo
 */
public class RdkitRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitRestRouteBuilder.class.getName());

    private static final ServiceDescriptor[] calculatorsServiceDescriptor
            = new ServiceDescriptor[]{new ServiceDescriptor(
                        "rdkit.calculators",
                        "RDKit LogP",
                        "RDKit LogP prediction",
                        new String[]{"logp", "partitioning", "molecularproperties", "rdkit"},
                        null,
                        new String[]{"/Chemistry/Toolkits/RDKit/Calculators", "Chemistry/Calculators/Partioning"},
                        "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
                        null,
                        new String[]{"public"},
                        MoleculeObject.class, // inputClass
                        MoleculeObject.class, // outputClass
                        Metadata.Type.ARRAY, // inputTypes
                        Metadata.Type.ARRAY, // outputTypes
                        new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "logp", // endpoint
                                    true, // URL is relative
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    0,
                                    Integer.MAX_VALUE,
                                    0f,
                                    null,
                                    null)
                        }
                )
            };

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("Ping\n")).endRest();

        rest("/v1/calculators").description("Property calculation services using RDKit")
                .bindingMode(RestBindingMode.off)
                .consumes("application/json")
                .produces("application/json")
                //
                // service descriptor
                .get().description("ServiceDescriptors for RDKit calculators")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(calculatorsServiceDescriptor);
                })
                .endRest()
                //
                .post("logp").description("Calculate the logP for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(RdkitCalculatorsRouteBuilder.RDKIT_LOGP)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest();

    }

}
