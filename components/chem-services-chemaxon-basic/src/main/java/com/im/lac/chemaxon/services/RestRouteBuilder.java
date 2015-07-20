package com.im.lac.chemaxon.services;

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
public class RestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RestRouteBuilder.class.getName());

    private static final ServiceDescriptor[] calculatorsServiceDescriptor
            = new ServiceDescriptor[]{new ServiceDescriptor(
                        "ChemAxon LogP",
                        "ChemAxon LogP. See https://www.chemaxon.com/products/calculator-plugins/property-predictors/#logp_logd",
                        new String[]{"logp", "partitioning", "chemaxon"},
                        null,
                        new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/Partioning"},
                        "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
                        null,
                        new String[]{"public"},
                        new Class[]{MoleculeObject.class}, // inputClasses
                        new Class[]{MoleculeObject.class}, // outputClasses
                        new Metadata.Type[]{Metadata.Type.ARRAY}, // inputTypes
                        new Metadata.Type[]{Metadata.Type.ARRAY}, // outputTypes
                        new AccessMode[]{
                            new AccessMode(
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "logp", // a URL relative to this URL?
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    0, Integer.MAX_VALUE, 0.001f,
                                    new ServiceDescriptor.LicenseToken[]{ServiceDescriptor.LicenseToken.CHEMAXON},
                            null)
                        }
                )
            };

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("ping")
                .get().description("Simple ping service to check things are running")
                .produces("text/plain")
                .route()
                .transform(constant("Ping\n")).endRest();

        rest("v1/calculators").description("Property calculation services using ChemAxon")
                .bindingMode(RestBindingMode.off)
                .consumes("application/json")
                .produces("application/json")
                //
                // service descriptor
                .get().description("ServiceDescriptors for ChemAxon calculators")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(calculatorsServiceDescriptor);
                })
                .endRest()
                //
                .post("logp").description("Calculate the calculated logP for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(CalculatorsRouteBuilder.CHEMAXON_LOGP)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("atomCount").description("Calculate the atom count for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(CalculatorsRouteBuilder.CHEMAXON_ATOM_COUNT)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("lipinski").description("Calculate the Lipinski properties for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(CalculatorsRouteBuilder.CHEMAXON_LIPINSKI)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("drugLikeFilter").description("Apply a drug like filter to the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(CalculatorsRouteBuilder.CHEMAXON_DRUG_LIKE_FILTER)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("chemTerms").description("Calculate a chemical terms expression for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(CalculatorsRouteBuilder.CHEMAXON_CHEMTERMS)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest();

        rest("v1/descriptors").description("Screening and clustering services using ChemAxon")
                .bindingMode(RestBindingMode.off)
                .consumes("application/json")
                .produces("application/json")
                //
                .post("screening/ecfp4").description("Screen using ECFP4 fingerprints")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(DescriptorsRouteBuilder.CHEMAXON_SCREENING_ECFP4)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("screening/pharmacophore").description("Screen using pharmacophore fingerprints")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(DescriptorsRouteBuilder.CHEMAXON_SCREENING_PHARMACOPHORE)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("clustering/spherex/ecfp4").description("Sphere exclusion clustering using ECFP4 fingerprints")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(DescriptorsRouteBuilder.CHEMAXON_CLUSTERING_SPHEREX_ECFP4)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest();

    }

}
