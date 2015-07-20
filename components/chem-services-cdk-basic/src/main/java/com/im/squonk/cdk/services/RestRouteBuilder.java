package com.im.squonk.cdk.services;

import com.im.lac.camel.util.CamelUtils;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.impl.ModeImpl;
import com.im.lac.services.impl.ServiceDescriptorImpl;
import com.im.lac.types.MoleculeObject;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 *
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RestRouteBuilder.class.getName());

    private static final ServiceDescriptor[] calculatorsServiceDescriptor
            = new ServiceDescriptor[]{new ServiceDescriptorImpl(
                        "CDK LogP",
                        "CDK LogP predictions for XLogP and ALogP",
                        new String[]{"logp", "partitioning", "cdk"},
                        null,
                        new String[]{"/Chemistry/Toolkits/CDK/Calculators", "Chemistry/Calculators/Partioning"},
                        "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
                        null,
                        new String[]{"public"},
                        new Class[]{MoleculeObject.class}, // inputClasses
                        new Class[]{MoleculeObject.class}, // outputClasses
                        new Metadata.Type[]{Metadata.Type.ARRAY}, // inputTypes
                        new Metadata.Type[]{Metadata.Type.ARRAY}, // outputTypes
                        new ServiceDescriptor.Mode[]{
                            new ModeImpl(
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "logp", // a URL relative to this URL?
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    0, Integer.MAX_VALUE, 0.001f,
                                    new ServiceDescriptor.LicenseToken[]{ServiceDescriptor.LicenseToken.CHEMAXON})
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

        rest("/v1/calculators").description("Property calculation services using CDK")
                .bindingMode(RestBindingMode.off)
                .consumes("application/json")
                .produces("application/json")
                //
                // service descriptor
                .get().description("ServiceDescriptors for CDK calculators")
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
                .to(CalculatorsRouteBuilder.CDK_LOGP)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest();

    }

}
