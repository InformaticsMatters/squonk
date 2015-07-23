package com.im.lac.chemaxon.services;

import com.im.lac.camel.util.CamelUtils;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.services.AccessMode;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.ServicePropertyDescriptor;
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

    private static final String NAME_SIM_CUTTOFF = "Similarity Cuttoff";
    private static final String DESC_SIM_CUTTOFF = "Similarity score cuttoff between 0 and 1 (1 means identical";
    private static final String NAME_QMOL = "Query Structure";
    private static final String DESC_QMOL = "Structure to us as the query";
    private static final String NAME_CT_EXPR = "ChemTerms Expression";
    private static final String DESC_CT_EXPR = "Expression using  the Chemical Terms language";

    private static final ServiceDescriptor[] SERVICE_DESCRIPTOR_CALCULATORS
            = new ServiceDescriptor[]{
                createServiceDescriptor(
                        "ChemAxon LogP",
                        "LogP using ChemAxon calculators. See http://www.chemaxon.com/products/calculator-plugins/property-predictors/#logp_logd",
                        new String[]{"logp", "partitioning", "molecularproperties", "chemaxon"},
                        new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/Partioning"},
                        "logp",
                        0.001f,
                        null),
                createServiceDescriptor(
                        "ChemAxon Atom Count",
                        "Atom Count using ChemAxon calculators. See http://www.chemaxon.com/products/calculator-plugins/property-calculations/#topology_analysis",
                        new String[]{"atomcount", "topology", "molecularproperties", "chemaxon"},
                        new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/Topological"},
                        "atomCount",
                        0f,
                        null),
                createServiceDescriptor(
                        "ChemAxon Lipinski Properties",
                        "Lipinski properties using ChemAxon calculators",
                        new String[]{"lipinski", "druglike", "molecularproperties", "chemaxon"},
                        new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/DrugLike"},
                        "lipinski",
                        0.002f,
                        null),
                createServiceDescriptor(
                        "ChemAxon Drug-like Filter",
                        "Drug-like filter using ChemAxon calculators. mass() < 400 and ringCount > 0 and rotatableBondCount() < 5 and acceptorCount <= 10 and LogP < 5",
                        new String[]{"druglike", "molecularproperties", "chemaxon"},
                        new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/DrugLike"},
                        "drugLikeFilter",
                        0.0025f,
                        null),
                createServiceDescriptor(
                        "ChemAxon Chemical Terms",
                        "Property prediction using a user definable Chemical Terms experssion. See http://docs.chemaxon.com/display/chemicalterms/Chemical+Terms+Home",
                        new String[]{"molecularproperties", "chemaxon"},
                        new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/General"},
                        "chemTerms",
                        0.01f,
                        new ServicePropertyDescriptor[]{
                            new ServicePropertyDescriptor(ServicePropertyDescriptor.Type.STRING, NAME_CT_EXPR, DESC_CT_EXPR)
                        })
            };

    private static final ServiceDescriptor[] SERVICE_DESCRIPTOR_DESCRIPTORS
            = new ServiceDescriptor[]{
                createServiceDescriptor(
                        "ChemAxon ECFP4 Screening",
                        "Virtual screening using ChemAxon ECFP4 fingerprints. See http://www.chemaxon.com/products/screen/",
                        new String[]{"virtualscreening", "screening", "ecfp", "ecfp4", "moleculardescriptors", "fingerprints", "chemaxon"},
                        new String[]{"/Vendors/ChemAxon/Screening", "Chemistry/Screening"},
                        "screening/ecfp4",
                        0.001f,
                        new ServicePropertyDescriptor[]{
                            new ServicePropertyDescriptor(ServicePropertyDescriptor.Type.FLOAT, NAME_QMOL, DESC_QMOL),
                            new ServicePropertyDescriptor(ServicePropertyDescriptor.Type.FLOAT, NAME_SIM_CUTTOFF, DESC_SIM_CUTTOFF)
                        }),
                createServiceDescriptor(
                        "ChemAxon Pharmacophore Screening",
                        "Virtual screening using ChemAxon 2D pharmacophore fingerprints. See http://www.chemaxon.com/products/screen/",
                        new String[]{"virtualscreening", "screening", "parmacophore", "moleculardescriptors", "fingerprints", "chemaxon"},
                        new String[]{"/Vendors/ChemAxon/Screening", "Chemistry/Screening"},
                        "screening/pharmacophore",
                        0.004f,
                        new ServicePropertyDescriptor[]{
                            new ServicePropertyDescriptor(ServicePropertyDescriptor.Type.FLOAT, NAME_QMOL, DESC_QMOL),
                            new ServicePropertyDescriptor(ServicePropertyDescriptor.Type.FLOAT, NAME_SIM_CUTTOFF, DESC_SIM_CUTTOFF)
                        }),
                createServiceDescriptor(
                        "ChemAxon SpereEx Clustering",
                        "Sphere exclusion clustering using ChemAxon ECFP4 fingerprints. See http://www.chemaxon.com/products/jkulstor/",
                        new String[]{"clustering", "ecfp", "ecfp4", "chemaxon"},
                        new String[]{"/Vendors/ChemAxon/Clustering", "Chemistry/Clustering"},
                        "clustering/spherex/ecfp4",
                        0.002f,
                        new ServicePropertyDescriptor[]{
                            new ServicePropertyDescriptor(ServicePropertyDescriptor.Type.FLOAT, NAME_SIM_CUTTOFF, DESC_SIM_CUTTOFF)
                        })
            };

    static ServiceDescriptor createServiceDescriptor(String name, String desc, String[] tags, String[] paths, String endpoint, float cost, ServicePropertyDescriptor[] props) {
        return new ServiceDescriptor(
                name,
                desc,
                tags,
                null,
                paths,
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
                            endpoint,
                            true, // a relative URL
                            AsyncHttpProcessDatasetJobDefinition.class,
                            0,
                            Integer.MAX_VALUE,
                            cost,
                            new ServiceDescriptor.LicenseToken[]{ServiceDescriptor.LicenseToken.CHEMAXON},
                            null)
                }
        );
    }

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
                    exch.getIn().setBody(SERVICE_DESCRIPTOR_CALCULATORS);
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
                // service descriptor
                .get().description("ServiceDescriptors for ChemAxon descriptors")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(SERVICE_DESCRIPTOR_DESCRIPTORS);
                })
                .endRest()
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
