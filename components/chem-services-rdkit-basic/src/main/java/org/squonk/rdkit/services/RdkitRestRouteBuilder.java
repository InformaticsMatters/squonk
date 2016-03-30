package org.squonk.rdkit.services;

import org.squonk.camel.processor.MoleculeObjectDatasetHttpProcessor;
import org.squonk.options.OptionDescriptor;
import org.squonk.camel.util.CamelUtils;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import com.im.lac.types.MoleculeObject;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.types.TypeResolver;

/**
 *
 * @author timbo
 */
public class RdkitRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitRestRouteBuilder.class.getName());

    private static final TypeResolver resolver = new TypeResolver();

    protected static final ServiceDescriptor[] CALCULATORS_SERVICE_DESCRIPTOR
            = new ServiceDescriptor[]{
                createServiceDescriptor(
                        "rdkit.calculators.logp",
                        "RDKit LogP",
                        "RDKit LogP prediction",
                        new String[]{"logp", "partitioning", "molecularproperties", "rdkit"},
                        "icons/properties_add.png",
                        new String[]{"/Chemistry/Toolkits/RDKit/Calculators", "/Chemistry/Calculators/Partioning"},
                        "asyncHttp",
                        "logp",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.frac_c_sp3",
                        "RDKit frac sp3 carbons",
                        "Fraction sp3 hybridised carbons using RDKit",
                        new String[]{"fraccsp3", "topology", "molecularproperties", "rdkit"},
                        "icons/properties_add.png",
                        new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Topological"},
                        "asyncHttp",
                        "frac_c_sp3",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.lipinski",
                        "RDKit Lipinski",
                        "Lipinski properties using RDKit",
                        new String[]{"lipinski", "hbond", "donors", "acceptors", "logp", "molecularweight", "druglike", "molecularproperties", "rdkit"},
                        "icons/properties_add.png",
                        new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/DrugLike"},
                        "asyncHttp",
                        "lipinski",
                        null,
                        null),
            createServiceDescriptor(
                    "rdkit.calculators.donors_acceptors",
                    "RDKit Donors & Acceptors",
                    "H-bond donor and acceptor counts using RDKit",
                    new String[]{"hbond", "donors", "acceptors", "druglike", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Topological"},
                    "asyncHttp",
                    "lipinski",
                    null,
                    null),
                createServiceDescriptor(
                        "rdkit.calculators.molar_refractivity",
                        "RDKit molar refractivity",
                        "Molar Refractivity using RDKit",
                        new String[]{"refractivity", "molarrefractivity", "molecularproperties", "rdkit"},
                        "icons/properties_add.png",
                        new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Other"},
                        "asyncHttp",
                        "molar_refractivity",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.tpsa",
                        "RDKit TPSA",
                        "Topological surface area using RDKit",
                        new String[]{"tpsa", "molarrefractivity", "molecularproperties", "rdkit"},
                        "icons/properties_add.png",
                        new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Other"},
                        "asyncHttp",
                        "tpsa",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.rings",
                        "RDKit rings",
                        "Ring count and aromatic ring count using RDKit",
                        new String[]{"rings", "aromatic", "molecularproperties", "rdkit"},
                        "icons/properties_add.png",
                        new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Topological"},
                        "asyncHttp",
                        "rings",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.rotatable_bonds",
                        "RDKit rotatable bond count",
                        "Rotatable bond count using RDKit",
                        new String[]{"rotatablebonds", "molecularproperties", "rdkit"},
                        "icons/properties_add.png",
                        new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Topological"},
                        "asyncHttp",
                        "rotatable_bonds",
                        null,
                        null)

            };

    static ServiceDescriptor createServiceDescriptor(String serviceDescriptorId, String name, String desc, String[] tags, String icon,
                                                     String[] paths, String modeId, String endpoint, OptionDescriptor[] props, String adapterClass) {
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
                Metadata.Type.STREAM, // inputType
                Metadata.Type.STREAM, // outputType
                icon,
                new AccessMode[]{
                    new AccessMode(
                            modeId,
                            "Immediate execution",
                            "Execute as an asynchronous REST web service",
                            endpoint,
                            true, // a relative URL
                            AsyncHttpProcessDatasetJobDefinition.class,
                            null,
                            null,
                            null,
                            null,
                            props,
                            adapterClass)
                }
        );
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();

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
                    exch.getIn().setBody(CALCULATORS_SERVICE_DESCRIPTOR);
                })
                .endRest()
                //
                .post("logp").description("Calculate the logP for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_LOGP, resolver))
                .endRest()
                //
                .post("frac_c_sp3").description("Calculate the fraction of SP3 hybrised carbons for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_FRACTION_C_SP3, resolver))
                .endRest()
                //
                .post("lipinski").description("Calculate Lipinski properties for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_LIPINSKI, resolver))
                .endRest()
                //
                .post("donors_acceptors").description("Calculate H-bond donor and acceptor counts for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_DONORS_ACCEPTORS, resolver))
                .endRest()
                //
                .post("molar_refractivity").description("Calculate molar refractivity for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_MOLAR_REFRACTIVITY, resolver))
                .endRest()
                //
                .post("tpsa").description("Calculate TPSA for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_TPSA, resolver))
                .endRest()
                //
                .post("rings").description("Calculate ring counts for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_RINGS, resolver))
                .endRest()
                //
                .post("rotatable_bonds").description("Calculate rotatable bond count for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectDatasetHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_ROTATABLE_BONDS, resolver))
                .endRest();

    }

}
