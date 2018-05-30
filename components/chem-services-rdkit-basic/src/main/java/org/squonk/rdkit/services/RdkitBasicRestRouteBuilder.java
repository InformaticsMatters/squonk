/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.rdkit.services;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.rdkit.io.RDKitMoleculeIOUtils.FragmentMode;
import org.squonk.types.NumberRange;
import org.squonk.types.TypeResolver;
import org.squonk.util.CommonConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;
import static org.squonk.rdkit.mol.EvaluatorDefinition.Function.*;

/**
 * @author timbo
 */
public class RdkitBasicRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitBasicRestRouteBuilder.class.getName());

    private static final String ROUTE_STATS = "seda:post_stats";

    private static final TypeResolver resolver = new TypeResolver();

    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.rdkit";

    protected static final HttpServiceDescriptor[] CALCULATORS_SERVICE_DESCRIPTOR
            = new HttpServiceDescriptor[]{
            createServiceDescriptor(
                    "rdkit.calculators.verify",
                    "Verify structure (RDKit)",
                    "Verify that the molecules are valid according to RDKit",
                    new String[]{"verify", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Verify+structure+%28RDKit%29",
                    "verify",
                    new OptionDescriptor[]{OptionDescriptor.FILTER_MODE_PASS},
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "rdkit.calculators.logp",
                    "LogP (RDKit)",
                    "RDKit LogP prediction",
                    new String[]{"logp", "partitioning", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogP+%28RDKit%29",
                    "logp",
                    null, null),
            createServiceDescriptor(
                    "rdkit.calculators.frac_c_sp3",
                    "Frac sp3 C (RDKit)",
                    "Fraction sp3 hybridised carbons using RDKit",
                    new String[]{"fraccsp3", "topology", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Frac+sp3+C+%28RDKit%29",
                    "frac_c_sp3",
                    null, null),
            createServiceDescriptor(
                    "rdkit.calculators.lipinski",
                    "Lipinski (RDKit)",
                    "Lipinski rule of 5 filter using RDKit",
                    new String[]{"lipinski", "ruleoffive", "ro5", "hbond", "donors", "acceptors", "logp", "molecularweight", "druglike", "molecularproperties", "filter", "rdkit"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Lipinski+filter+%28RDKit%29",
                    "lipinski",
                    createLipinskiOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "rdkit.calculators.ruleofthree",
                    "Rule of 3 (RDKit)",
                    "Rule of 3 filter using RDKit",
                    new String[]{"ruleofthree", "ro3", "hbond", "donors", "acceptors", "logp", "molecularweight", "rotatablebonds", "leadlike", "molecularproperties", "filter", "rdkit"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Rule+of+3+filter+%28RDKit%29",
                    "ruleofthree",
                    createRuleOfThreeOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "rdkit.calculators.ghose",
                    "Ghose filter (RDKit)",
                    "Ghose filter using RDKit",
                    new String[]{"ghose", "logp", "molecularweight", "atomcount", "druglike", "molecularproperties", "molarrefractivity", "refractivity", "filter", "rdkit"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Ghose+filter+%28RDKit%29",
                    "ghose",
                    createGhoseFilterOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "rdkit.calculators.veber",
                    "Veber filter (RDKit)",
                    "Veber filter using RDKit",
                    new String[]{"veber", "druglike", "molecularproperties", "rotatablebonds", "tpsa", "psa", "filter", "rdkit"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Veber+filter+%28RDKit%29",
                    "veber",
                    createVeberFilterOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "rdkit.calculators.reos",
                    "REOS (RDKit)",
                    "Rapid Elimination Of Swill (REOS) using RDKit",
                    new String[]{"reos", "hbond", "donors", "acceptors", "logp", "molecularweight", "rotatablebonds", "charge", "formalcharge", "leadlike", "molecularproperties", "filter", "rdkit"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/REOS+Filter+%28RDKit%29",
                    "reos",
                    createReosOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "rdkit.calculators.donors_acceptors",
                    "HBA & HBD (RDKit)",
                    "H-bond donor and acceptor counts using RDKit",
                    new String[]{"hbond", "donors", "acceptors", "druglike", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/HBA+%26+HBD+%28RDKit%29",
                    "donors_acceptors",
                    null, null),
            createServiceDescriptor(
                    "rdkit.calculators.molar_refractivity",
                    "Molar Refractivity (RDKit)",
                    "Molar Refractivity using RDKit",
                    new String[]{"refractivity", "molarrefractivity", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Molar+Refractivity+%28RDKit%29",
                    "molar_refractivity",
                    null, null),
            createServiceDescriptor(
                    "rdkit.calculators.tpsa",
                    "TPSA (RDKit)",
                    "Topological surface area using RDKit",
                    new String[]{"tpsa", "psa", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/TPSA+%28RDKit%29",
                    "tpsa",
                    null, null),
            createServiceDescriptor(
                    "rdkit.calculators.rings",
                    "Ring Count (RDKit)",
                    "Ring count and aromatic ring count using RDKit",
                    new String[]{"rings", "aromatic", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Rotatable+bonds+%28RDKit%29",
                    "rings",
                    null, null),
            createServiceDescriptor(
                    "rdkit.calculators.rotatable_bonds",
                    "Rotatable bonds (RDKit)",
                    "Rotatable bond count using RDKit",
                    new String[]{"rotatablebonds", "molecularproperties", "rdkit"},
                    "icons/properties_add.png",
                    "",
                    "rotatable_bonds",
                    null, null),
            createServiceDescriptor(
                    "rdkit.calculators.canonical_smiles",
                    "Canonical Smiles (RDKit)",
                    "Canonical Smiles using RDKit",
                    new String[]{"smiles", "canonical", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Canonical+Smiles+%28RDKit%29",
                    "canonical_smiles",
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(String.class, "query.mode", "Fragment mode", "How to handle molecules with multiple fragments", Mode.User)
                                    .withValues(fragmentModesToStringArray())
                    },
                    null),
            createServiceDescriptor(
                    "rdkit.calculators.formal_charge",
                    "Formal Charge (RDKit)",
                    "Formal Charge using RDKit",
                    new String[]{"charge", "formalcharge", "rdkit"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Formal+Charge+%28RDKit%29",
                    "formal_charge",
                    null, null)

    };

    static private OptionDescriptor[] createLipinskiOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(1)
                .withMinMaxValues(1, 1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EXACT_MW.getName(),
                "Mol weight", "Molecular weight", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(0f, 500f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + LOGP.getName(),
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(null, 5.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + LIPINSKI_HBD.getName(),
                "HBD count", "H-bond donor count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 5)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + LIPINSKI_HBA.getName(),
                "HBA count", "H-bond acceptor count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 10)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createRuleOfThreeOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1, 1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + LOGP.getName(),
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(null, 3.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EXACT_MW.getName(),
                "Mol weight", "Molecular weight", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(0f, 300f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + NUM_HBD.getName(),
                "HBD count", "H-bond donor count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 3)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + NUM_HBA.getName(),
                "HBA count", "H-bond acceptor count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 3)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + NUM_ROTATABLE_BONDS.getName(),
                "Rot bond count", "Rotatable bond count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 3)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createGhoseFilterOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1, 1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + LOGP.getName(),
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(-0.4f, 5.6f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EXACT_MW.getName(),
                "Mol weight", "Molecular weight", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(160f, 480f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ATOM_COUNT.getName(),
                "Atom count", "Atom count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(20, 70)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + MOLAR_REFRACTIVITY.getName(),
                "Refractivity", "Molar Refractivity", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(40f, 130f)));

        return list.toArray(new OptionDescriptor[0]);
    }


    static private OptionDescriptor[] createReosOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1, 1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + EXACT_MW.getName(),
                "Mol weight", "Molecular weight", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(200f, 500f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + LOGP.getName(),
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(-5.0f, 5.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + NUM_HBD.getName(),
                "HBD count", "H-bond donor count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 5)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + NUM_HBA.getName(),
                "HBA count", "H-bond acceptor count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 10)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + FORMAL_CHARGE.getName(),
                "Formal charge", "Formal charge", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(-2, 2)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + NUM_ROTATABLE_BONDS.getName(),
                "Rot bond count", "Rotatable bond count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 8)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + HEAVY_ATOM_COUNT.getName(),
                "Heavy atom count", "Heavy atom count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(15, 50)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createVeberFilterOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1, 1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + TPSA.getName(),
                "TPSA", "Topological polar surface area", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Float(0f, 140f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + NUM_ROTATABLE_BONDS.getName(),
                "Rot bond count", "Rotatable bond count", Mode.User).withMinMaxValues(0, 1).withDefaultValue(new NumberRange.Integer(0, 10)));

        return list.toArray(new OptionDescriptor[0]);
    }


    static String[] fragmentModesToStringArray() {

        String[] values = new String[FragmentMode.values().length];
        int i = 0;
        for (FragmentMode mode : FragmentMode.values()) {
            values[i] = mode.toString();
            i++;
        }
        return values;
    }


    static HttpServiceDescriptor createServiceDescriptor(
            String serviceDescriptorId, String name, String desc, String[] tags, String icon, String resourceUrl,
            String endpoint, OptionDescriptor[] options, ThinDescriptor thinDescriptor) {

        return new HttpServiceDescriptor(
                serviceDescriptorId,
                name,
                desc,
                tags,
                resourceUrl,
                icon,
                IODescriptors.createMoleculeObjectDataset("input"),
                IODescriptors.createMoleculeObjectDataset("output"),
                options,
                thinDescriptor,
                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME,
                endpoint
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
                .post("ghose").description("Ghose filter for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_GHOSE_FILTER, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("veber").description("Veber filter for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(RdkitCalculatorsRouteBuilder.RDKIT_VEBER_FILTER, resolver, ROUTE_STATS))
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
