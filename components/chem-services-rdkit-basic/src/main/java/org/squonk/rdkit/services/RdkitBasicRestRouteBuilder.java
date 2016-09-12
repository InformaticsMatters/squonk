package org.squonk.rdkit.services;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptor.DataType;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.options.OptionDescriptor;
import org.squonk.rdkit.io.RDKitMoleculeIOUtils.FragmentMode;
import org.squonk.rdkit.mol.EvaluatorDefinition;
import org.squonk.types.MoleculeObject;
import org.squonk.types.NumberRange;
import org.squonk.types.TypeResolver;
import org.squonk.util.CommonConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;

/**
 * @author timbo
 */
public class RdkitBasicRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitBasicRestRouteBuilder.class.getName());

    private static final String ROUTE_STATS = "seda:post_stats";

    private static final TypeResolver resolver = new TypeResolver();

    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.rdkit";

    protected static final ServiceDescriptor[] CALCULATORS_SERVICE_DESCRIPTOR
            = new ServiceDescriptor[]{
            createServiceDescriptor(
                    "rdkit.calculators.verify",
                    "Verify structure (RDKit)",
                    "Verify that the molecules are valid according to RDKit",
                    new String[]{"verify", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Chemistry/Toolkits/RDKit/Verify", "/Chemistry/Verify"},
                    "asyncHttp",
                    "verify",
                    new OptionDescriptor[] {OptionDescriptor.IS_FILTER, OptionDescriptor.FILTER_MODE}),
            createServiceDescriptor(
                    "rdkit.calculators.logp",
                    "LogP (RDKit)",
                    "RDKit LogP prediction",
                    new String[]{"logp", "partitioning", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Chemistry/Toolkits/RDKit/Calculators", "/Chemistry/Calculators/Partioning"},
                    "asyncHttp",
                    "logp",
                    null),
            createServiceDescriptor(
                    "rdkit.calculators.frac_c_sp3",
                    "Frac sp3 C (RDKit)",
                    "Fraction sp3 hybridised carbons using RDKit",
                    new String[]{"fraccsp3", "topology", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Topological"},
                    "asyncHttp",
                    "frac_c_sp3",
                    null),
            createServiceDescriptor(
                    "rdkit.calculators.lipinski",
                    "Lipinski (RDKit)",
                    "Lipinski rule of 5 filter using RDKit",
                    new String[]{"lipinski", "ruleoffive", "ro5", "hbond", "donors", "acceptors", "logp", "molecularweight", "druglike", "molecularproperties", "filter", "rdkit"},
                    "icons/filter_molecules.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/DrugLike"},
                    "asyncHttp",
                    "lipinski",
                    createLipinskiOptionDescriptors()),
            createServiceDescriptor(
                    "rdkit.calculators.ruleofthree",
                    "Rule of 3 (RDKit)",
                    "Rule of 3 filter using RDKit",
                    new String[]{"ruleofthree", "ro3", "hbond", "donors", "acceptors", "logp", "molecularweight", "rotatablebonds", "leadlike", "molecularproperties", "filter", "rdkit"},
                    "icons/filter_molecules.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/DrugLike"},
                    "asyncHttp",
                    "ruleofthree",
                    createRuleOfThreeOptionDescriptors()),
            createServiceDescriptor(
                    "rdkit.calculators.reos",
                    "REOS (RDKit)",
                    "Rapid Elimination Of Swill (REOS) using RDKit",
                    new String[]{"reos", "hbond", "donors", "acceptors", "logp", "molecularweight", "rotatablebonds", "charge", "formalcharge", "leadlike", "molecularproperties", "filter", "rdkit"},
                    "icons/filter_molecules.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/DrugLike"},
                    "asyncHttp",
                    "reos",
                    createReosOptionDescriptors()),
            createServiceDescriptor(
                    "rdkit.calculators.donors_acceptors",
                    "HBA & HBD (RDKit)",
                    "H-bond donor and acceptor counts using RDKit",
                    new String[]{"hbond", "donors", "acceptors", "druglike", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Topological"},
                    "asyncHttp",
                    "donors_acceptors",
                    null),
            createServiceDescriptor(
                    "rdkit.calculators.molar_refractivity",
                    "Molar Refractivity (RDKit)",
                    "Molar Refractivity using RDKit",
                    new String[]{"refractivity", "molarrefractivity", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Other"},
                    "asyncHttp",
                    "molar_refractivity",
                    null),
            createServiceDescriptor(
                    "rdkit.calculators.tpsa",
                    "TPSA (RDKit)",
                    "Topological surface area using RDKit",
                    new String[]{"tpsa", "psa", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Other"},
                    "asyncHttp",
                    "tpsa",
                    null),
            createServiceDescriptor(
                    "rdkit.calculators.rings",
                    "Ring Count (RDKit)",
                    "Ring count and aromatic ring count using RDKit",
                    new String[]{"rings", "aromatic", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Topological"},
                    "asyncHttp",
                    "rings",
                    null),
            createServiceDescriptor(
                    "rdkit.calculators.rotatable_bonds",
                    "Rotatable bonds (RDKit)",
                    "Rotatable bond count using RDKit",
                    new String[]{"rotatablebonds", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Topological"},
                    "asyncHttp",
                    "rotatable_bonds",
                    null),
            createServiceDescriptor(
                    "rdkit.calculators.canonical_smiles",
                    "Canonical Smiles (RDKit)",
                    "Canonical Smiles using RDKit",
                    new String[]{"smiles", "canonical", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Other"},
                    "asyncHttp",
                    "canonical_smiles",
                    new OptionDescriptor[] {
                            new OptionDescriptor<>(String.class, "query.mode", "Fragment mode", "How to handle molecules with multiple fragments")
                                    .withValues(fragmentModesToStringArray())
                    }),
            createServiceDescriptor(
                    "rdkit.calculators.formal_charge",
                    "Formal Charge (RDKit)",
                    "Formal Charge using RDKit",
                    new String[]{"charge", "formalcharge", "rdkit"},
                    "icons/properties_add.png",
                    new String[]{"/Vendors/RDKit/Calculators", "/Chemistry/Calculators/Other"},
                    "asyncHttp",
                    "formal_charge",
                    null)

    };

    static private OptionDescriptor[] createLipinskiOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(new OptionDescriptor<>(Boolean.class, "option.filter", "filter mode", "filter mode").withDefaultValue(true).withAccess(false, false));
        list.add(new OptionDescriptor<>(String.class, "query." + CommonConstants.OPTION_FILTER_MODE, "Filter mode", "How to filter results")
                .withValues(new String[] {"INCLUDE_PASS", "INCLUDE_FAIL", "INCLUDE_ALL"}).withDefaultValue("INCLUDE_PASS")
                .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EvaluatorDefinition.Function.EXACT_MW.getName(),
                "MolWeight", "molecular weight").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(0f, 500f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EvaluatorDefinition.Function.LOGP.getName(),
                "LogP", "LogP partition coefficient").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(null, 5.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.LIPINSKI_HBD.getName(),
                "HBD count", "h-bond donor count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 5)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.LIPINSKI_HBA.getName(),
                "HBA count", "h-bond acceptor count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 10)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createRuleOfThreeOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(new OptionDescriptor<>(Boolean.class, "option.filter", "filter mode", "filter mode").withDefaultValue(true).withAccess(false, false));
        list.add(new OptionDescriptor<>(String.class, "query." + CommonConstants.OPTION_FILTER_MODE, "Filter mode", "How to filter results")
                .withValues(new String[] {"INCLUDE_PASS", "INCLUDE_FAIL", "INCLUDE_ALL"}).withDefaultValue("INCLUDE_PASS")
                .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EvaluatorDefinition.Function.LOGP.getName(),
                "LogP", "LogP partition coefficient").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(null, 3.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EvaluatorDefinition.Function.EXACT_MW.getName(),
                "MolWeight", "molecular weight").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(0f, 300f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.NUM_HBD.getName(),
                "HBD count", "h-bond donor count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 3)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.NUM_HBA.getName(),
                "HBA count", "h-bond acceptor count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 3)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.NUM_ROTATABLE_BONDS.getName(),
                "Rot bond count", "rotatable bond count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 3)));

        return list.toArray(new OptionDescriptor[0]);
    }


    static private OptionDescriptor[] createReosOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(new OptionDescriptor<>(Boolean.class, "option.filter", "filter mode", "filter mode").withDefaultValue(true).withAccess(false, false));
        list.add(new OptionDescriptor<>(String.class, "query." + CommonConstants.OPTION_FILTER_MODE, "Filter mode", "How to filter results")
                        .withValues(new String[] {"INCLUDE_PASS", "INCLUDE_FAIL", "INCLUDE_ALL"}).withDefaultValue("INCLUDE_PASS")
                        .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EvaluatorDefinition.Function.EXACT_MW.getName(),
                "MolWeight", "molecular weight").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(200f, 500f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EvaluatorDefinition.Function.LOGP.getName(),
                "LogP", "LogP partition coefficient").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(-5.0f, 5.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.NUM_HBD.getName(),
                "HBD count", "h-bond donor count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 5)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.NUM_HBA.getName(),
                "HBA count", "h-bond acceptor count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 10)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.FORMAL_CHARGE.getName(),
                "Formal charge", "formal charge").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(-2, 2)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.NUM_ROTATABLE_BONDS.getName(),
                "Rot bond count", "rotatable bond count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 8)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + EvaluatorDefinition.Function.HEAVY_ATOM_COUNT.getName(),
                "Heavy atom count", "heavy atom count").withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(15, 50)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static String[] fragmentModesToStringArray() {

        String[] values = new String[FragmentMode.values().length];
        int i=0;
        for (FragmentMode mode: FragmentMode.values()) {
            values[i] = mode.toString();
            i++;
        }
        return values;
    }


    static ServiceDescriptor createServiceDescriptor(String serviceDescriptorId, String name, String desc, String[] tags, String icon,
                                                     String[] paths, String modeId, String endpoint, OptionDescriptor[] props) {
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
                                null,
                                null,
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
                .post("verify").description("Verify as RDKit molecules")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_STRUCTURE_VERIFY, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("logp").description("Calculate the logP for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_LOGP, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("frac_c_sp3").description("Calculate the fraction of SP3 hybridised carbons for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_FRACTION_C_SP3, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("lipinski").description("Lipinski filter for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_LIPINSKI, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("ruleofthree").description("Rule of three filter for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_RULE_OF_THREE, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("reos").description("REOS filter for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_REOS, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("donors_acceptors").description("Calculate H-bond donor and acceptor counts for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_DONORS_ACCEPTORS, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("molar_refractivity").description("Calculate molar refractivity for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_MOLAR_REFRACTIVITY, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("tpsa").description("Calculate TPSA for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_TPSA, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("rings").description("Calculate ring counts for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_RINGS, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("rotatable_bonds").description("Calculate rotatable bond count for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_ROTATABLE_BONDS, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("canonical_smiles").description("Generate canonical smiles for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_CANONICAL_SMILES, resolver, ROUTE_STATS))
                .endRest();

    }

}
