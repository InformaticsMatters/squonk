package org.squonk.rdkit.services;

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
public class RdkitRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitRestRouteBuilder.class.getName());

    private static final ServiceDescriptor[] calculatorsServiceDescriptor
            = new ServiceDescriptor[]{
                createServiceDescriptor(
                        "rdkit.calculators.logp",
                        "RDKit LogP",
                        "RDKit LogP prediction",
                        new String[]{"logp", "partitioning", "molecularproperties", "rdkit"},
                        new String[]{"/Chemistry/Toolkits/RDKit/Calculators", "Chemistry/Calculators/Partioning"},
                        "asyncHttp",
                        "logp",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.frac_c_sp3",
                        "RDKit frac sp3 carbons",
                        "Fraction sp3 hybridised carbons using RDKit",
                        new String[]{"fraccsp3", "topology", "molecularproperties", "rdkit"},
                        new String[]{"/Vendors/RDKit/Calculators", "Chemistry/Calculators/Topological"},
                        "asyncHttp",
                        "frac_c_sp3",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.lipinski",
                        "RDKit Lipinski",
                        "Lipinski properties using RDKit",
                        new String[]{"lipinski", "druglike", "molecularproperties", "rdkit"},
                        new String[]{"/Vendors/RDKit/Calculators", "Chemistry/Calculators/DrugLike"},
                        "asyncHttp",
                        "lipinski",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.molar_refractivity",
                        "RDKit molar refractivity",
                        "Molar Refractivity using RDKit",
                        new String[]{"refractivity", "molarrefractivity", "molecularproperties", "rdkit"},
                        new String[]{"/Vendors/RDKit/Calculators", "Chemistry/Calculators/Other"},
                        "asyncHttp",
                        "molar_refractivity",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.tpsa",
                        "RDKit TPSA",
                        "Topological surface area using RDKit",
                        new String[]{"tpsa", "molarrefractivity", "molecularproperties", "rdkit"},
                        new String[]{"/Vendors/RDKit/Calculators", "Chemistry/Calculators/Other"},
                        "asyncHttp",
                        "tpsa",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.rings",
                        "RDKit rings",
                        "Ring count and aromatic ring count using RDKit",
                        new String[]{"rings", "aromatic", "molecularproperties", "rdkit"},
                        new String[]{"/Vendors/RDKit/Calculators", "Chemistry/Calculators/Topological"},
                        "asyncHttp",
                        "rings",
                        null,
                        null),
                createServiceDescriptor(
                        "rdkit.calculators.rotatable_bonds",
                        "RDKit rotatable bond count",
                        "Rotatable bond count using RDKit",
                        new String[]{"rotatablebonds", "molecularproperties", "rdkit"},
                        new String[]{"/Vendors/RDKit/Calculators", "Chemistry/Calculators/Topological"},
                        "asyncHttp",
                        "rotatable_bonds",
                        null,
                        null)

            };

    static ServiceDescriptor createServiceDescriptor(String serviceDescriptorId, String name, String desc, String[] tags,
            String[] paths, String modeId, String endpoint, ServicePropertyDescriptor[] props, String adapterClass) {
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
                Metadata.Type.ARRAY, // inputType
                Metadata.Type.ARRAY, // outputType
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
                .endRest()
                //
                .post("frac_c_sp3").description("Calculate the fraction of SP3 hybrised carbons for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(RdkitCalculatorsRouteBuilder.RDKIT_FRACTION_C_SP3)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("lipinski").description("Calculate Lipinski properties for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(RdkitCalculatorsRouteBuilder.RDKIT_LIPINSKI)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("molar_refractivity").description("Calculate molar refractivity for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(RdkitCalculatorsRouteBuilder.RDKIT_MOLAR_REFRACTIVITY)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("tpsa").description("Calculate TPSA for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(RdkitCalculatorsRouteBuilder.RDKIT_TPSA)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("rings").description("Calculate ring counts for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(RdkitCalculatorsRouteBuilder.RDKIT_RINGS)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest()
                //
                .post("rotatable_bonds").description("Calculate rotatable bond count for the supplied MoleculeObjects")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(RdkitCalculatorsRouteBuilder.RDKIT_ROTATABLE_BONDS)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest();

    }

}
