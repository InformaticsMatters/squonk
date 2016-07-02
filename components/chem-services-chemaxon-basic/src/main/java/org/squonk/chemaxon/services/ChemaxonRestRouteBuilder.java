package org.squonk.chemaxon.services;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.chemaxon.processor.clustering.SphereExclusionClusteringProcessor;
import org.squonk.camel.chemaxon.processor.screening.MoleculeScreenerProcessor;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptor.DataType;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;
import org.squonk.types.TypeResolver;

import java.util.logging.Logger;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;

/**
 * @author timbo
 */
public class ChemaxonRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(ChemaxonRestRouteBuilder.class.getName());

    private static final String ROUTE_STATS = "seda:post_stats";

    private static final String HEADER = "header.";
    private static final String KEY_SIM_CUTTOFF = HEADER + MoleculeScreenerProcessor.HEADER_THRESHOLD;
    private static final String LABEL_SIM_CUTTOFF = "Similarity Cuttoff";
    private static final String DESC_SIM_CUTTOFF = "Similarity score cuttoff between 0 and 1 (1 means identical)";
    private static final String KEY_QMOL = HEADER + MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE;
    private static final String LABEL_QMOL = "Query Structure";
    private static final String DESC_QMOL = "Structure to use as the query";
    //    private static final String KEY_CT_EXPR = "ct_expr";
//    private static final String LABEL_CT_EXPR = "ChemTerms Expression";
//    private static final String DESC_CT_EXPR = "Expression using  the Chemical Terms language";
    private static final String KEY_MIN_CLUSTERS = HEADER + SphereExclusionClusteringProcessor.HEADER_MIN_CLUSTER_COUNT;
    private static final String LABEL_MIN_CLUSTERS = "Min clusters";
    private static final String DESC_MIN_CLUSTERS = "Minimum number of clusters to generate";
    private static final String KEY_MAX_CLUSTERS = HEADER + SphereExclusionClusteringProcessor.HEADER_MAX_CLUSTER_COUNT;
    private static final String LABEL_MAX_CLUSTERS = "Max clusters";
    private static final String DESC_MAX_CLUSTERS = "Target maximum number of clusters to generate";

    private static final TypeResolver resolver = new TypeResolver();

    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.chemaxon";

    protected static final ServiceDescriptor[] SERVICE_DESCRIPTOR_CALCULATORS
            = new ServiceDescriptor[]{
            createServiceDescriptor(
                    "chemaxon.calculators.verify",
                    "Verify structure (ChemAxon)",
                    "Verify that the molecules are valid according to ChemAxon's Marvin",
                    new String[]{"verify", "chemaxon"},
                    "icons/properties_add.png",
                    new String[]{"/Chemistry/Toolkits/RDKit/Verify", "/Chemistry/Verify"},
                    "asyncHttp",
                    "verify",
                    0f,
                    new OptionDescriptor[] {OptionDescriptor.IS_FILTER, OptionDescriptor.FILTER_MODE}),
            createServiceDescriptor(
                    "chemaxon.calculators.logp",
                    "LogP (CXN)",
                    "LogP using ChemAxon calculators. See http://www.chemaxon.com/products/calculator-plugins/property-predictors/#logp_logd",
                    new String[]{"logp", "partitioning", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/Partioning"},
                    "asyncHttp",
                    "logp",
                    0.001f,
                    null),
            createServiceDescriptor(
                    "chemaxon.calculators.atomcount",
                    "Atom Count (CXN)",
                    "Atom Count using ChemAxon calculators. See http://www.chemaxon.com/products/calculator-plugins/property-calculations/#topology_analysis",
                    new String[]{"atomcount", "topology", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/Topological"},
                    "asyncHttp",
                    "atomCount",
                    0f,
                    null),
            createServiceDescriptor(
                    "chemaxon.calculators.lipinski",
                    "Lipinski (CXN)",
                    "Lipinski properties using ChemAxon calculators",
                    new String[]{"lipinski", "druglike", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/DrugLike"},
                    "asyncHttp",
                    "lipinski",
                    0.002f,
                    null),
            createServiceDescriptor(
                    "chemaxon.calculators.druglikefilter",
                    "Drug-like Filter (CXN)",
                    "Drug-like filter using ChemAxon calculators. mass() < 400 and ringCount > 0 and rotatableBondCount() < 5 and acceptorCount <= 10 and LogP < 5",
                    new String[]{"druglike", "molecularproperties", "chemaxon"},
                    "icons/properties_filter.png",
                    new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/DrugLike"},
                    "asyncHttp",
                    "drugLikeFilter",
                    0.0025f,
                    null)//,
//                createServiceDescriptor(
//                        "chemaxon.calculators.chemterms",
//                        "CXN Chemical Terms",
//                        "Property prediction using a user definable Chemical Terms experssion. See http://docs.chemaxon.com/display/chemicalterms/Chemical+Terms+Home",
//                        new String[]{"molecularproperties", "chemaxon"},
//                        new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/General"},
//                        "asyncHttp",
//                        "chemTerms",
//                        0.01f,
//                        new ServicePropertyDescriptor[]{
//                            new ServicePropertyDescriptor(ServicePropertyDescriptor.Type.STRING, KEY_CT_EXPR, LABEL_CT_EXPR, DESC_CT_EXPR)
//                        })
    };

    private static final ServiceDescriptor[] SERVICE_DESCRIPTOR_DESCRIPTORS
            = new ServiceDescriptor[]{
            createServiceDescriptor(
                    "chemaxon.screening.ecpf4",
                    "ECFP4 Screen (CXN)",
                    "Virtual screening using ChemAxon ECFP4 fingerprints. See http://www.chemaxon.com/products/screen/",
                    new String[]{"virtualscreening", "screening", "ecfp", "ecfp4", "moleculardescriptors", "fingerprints", "chemaxon"},
                    "icons/filter_molecules.png",
                    new String[]{"/Vendors/ChemAxon/Screening", "Chemistry/Screening"},
                    "asyncHttp",
                    "screening/ecfp4",
                    0.001f,
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(Boolean.class, "option.filter", "filter mode", "filter mode").withDefaultValue(true).withAccess(false, false),
                            new OptionDescriptor<>(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.DISCRETE, new String[]{"smiles"}), KEY_QMOL, LABEL_QMOL, DESC_QMOL),
                            new OptionDescriptor<>(Float.class, KEY_SIM_CUTTOFF, LABEL_SIM_CUTTOFF, DESC_SIM_CUTTOFF).withDefaultValue(0.7f)
                    }),
            createServiceDescriptor(
                    "chemaxon.screening.pharmacophore",
                    "Pharmacophore Screen (CXN)",
                    "Virtual screening using ChemAxon 2D pharmacophore fingerprints. See http://www.chemaxon.com/products/screen/",
                    new String[]{"virtualscreening", "screening", "parmacophore", "moleculardescriptors", "fingerprints", "chemaxon"},
                    "icons/filter_molecules.png",
                    new String[]{"/Vendors/ChemAxon/Screening", "Chemistry/Screening"},
                    "asyncHttp",
                    "screening/pharmacophore",
                    0.004f,
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(Boolean.class, "option.filter", "filter mode", "filter mode").withDefaultValue(true).withAccess(false, false),
                            new OptionDescriptor<>(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.DISCRETE, new String[]{"smiles"}), KEY_QMOL, LABEL_QMOL, DESC_QMOL),
                            new OptionDescriptor<>(Float.class, KEY_SIM_CUTTOFF, LABEL_SIM_CUTTOFF, DESC_SIM_CUTTOFF).withDefaultValue(0.7f)
                    }),
            createServiceDescriptor(
                    "chemaxon.clustering.sperex",
                    "SpereEx Clustering (CXN)",
                    "Sphere exclusion clustering using ChemAxon ECFP4 fingerprints. See http://www.chemaxon.com/products/jklustor/",
                    new String[]{"clustering", "ecfp", "ecfp4", "chemaxon"},
                    "icons/clustering.png",
                    new String[]{"/Vendors/ChemAxon/Clustering", "Chemistry/Clustering"},
                    "asyncHttp",
                    "clustering/spherex/ecfp4",
                    0.002f,
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(Integer.class, KEY_MIN_CLUSTERS, LABEL_MIN_CLUSTERS, DESC_MIN_CLUSTERS).withDefaultValue(5),
                            new OptionDescriptor<>(Integer.class, KEY_MAX_CLUSTERS, LABEL_MAX_CLUSTERS, DESC_MAX_CLUSTERS).withDefaultValue(10)
                    })
    };

    static ServiceDescriptor createServiceDescriptor(String serviceDescriptorId, String name, String desc, String[] tags, String icon,
                                                     String[] paths, String modeId, String endpoint, float cost, OptionDescriptor[] props) {
        return new ServiceDescriptor(
                serviceDescriptorId,
                name,
                desc,
                tags,
                null,
                paths,
                "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
                null,
                new String[]{"public"},
                MoleculeObject.class, // inputClass
                MoleculeObject.class, // outputClass
                DataType.STREAM, // inputType
                DataType.STREAM, // outputType
                icon,
                new AccessMode[]{
                        new AccessMode(
                                modeId,
                                "Immediate execution",
                                "Execute as an asynchronous REST web service",
                                endpoint,
                                true, // a relative URL
                                null,
                                null,
                                cost,
                                new ServiceDescriptor.LicenseToken[]{ServiceDescriptor.LicenseToken.CHEMAXON},
                                props,
                                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                }
        );
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        // send usage metrics to the message queue
        from(ROUTE_STATS)
                .marshal().json(JsonLibrary.Jackson)
                .to(mqueueUrl);

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("ping")
                .get().description("Simple ping service to check things are running")
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();

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
                .post("verify").description("Verify as Marvin molecules")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_STRUCTURE_VERIFY, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("logp").description("Calculate the logP for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_LOGP, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("atomCount").description("Calculate the atom count for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_ATOM_COUNT, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("lipinski").description("Calculate the Lipinski properties for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_LIPINSKI, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("drugLikeFilter").description("Apply a drug like filter to the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_DRUG_LIKE_FILTER, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("chemTerms").description("Calculate a chemical terms expression for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_CHEMTERMS, resolver, ROUTE_STATS))
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
//                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
//                .to(ChemaxonDescriptorsRouteBuilder.CHEMAXON_SCREENING_ECFP4)
//                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonDescriptorsRouteBuilder.CHEMAXON_SCREENING_ECFP4, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("screening/pharmacophore").description("Screen using pharmacophore fingerprints")
                .route()
//                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
//                .to(ChemaxonDescriptorsRouteBuilder.CHEMAXON_SCREENING_PHARMACOPHORE)
//                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonDescriptorsRouteBuilder.CHEMAXON_SCREENING_PHARMACOPHORE, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("clustering/spherex/ecfp4").description("Sphere exclusion clustering using ECFP4 fingerprints")
                .route()
//                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
//                .to(ChemaxonDescriptorsRouteBuilder.CHEMAXON_CLUSTERING_SPHEREX_ECFP4)
//                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonDescriptorsRouteBuilder.CHEMAXON_CLUSTERING_SPHEREX_ECFP4, resolver, ROUTE_STATS))
                .endRest();

    }

}
