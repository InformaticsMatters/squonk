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

package org.squonk.chemaxon.services;

import chemaxon.jep.ChemJEP;
import chemaxon.jep.Evaluator;
import chemaxon.jep.context.MolContext;
import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import com.chemaxon.version.VersionInfo;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.chemaxon.processor.clustering.SphereExclusionClusteringProcessor;
import org.squonk.camel.chemaxon.processor.enumeration.ReactorProcessor;
import org.squonk.camel.chemaxon.processor.screening.MoleculeScreenerProcessor;
import org.squonk.camel.processor.DatasetToJsonProcessor;
import org.squonk.camel.processor.JsonToDatasetProcessor;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.chemaxon.molecule.ChemTermsEvaluator;
import org.squonk.chemaxon.molecule.MoleculeUtils;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.http.RequestInfo;
import org.squonk.io.IODescriptors;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.MoleculeObject;
import org.squonk.types.NumberRange;
import org.squonk.types.TypeResolver;
import org.squonk.util.CommonConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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

    protected static final HttpServiceDescriptor[] SERVICE_DESCRIPTOR_CALCULATORS
            = new HttpServiceDescriptor[]{
            createServiceDescriptor(
                    "chemaxon.calculators.verify",
                    "Verify structure (ChemAxon)",
                    "Verify that molecules are valid according to ChemAxon's Marvin",
                    new String[]{"verify", "chemaxon"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Verify+structure+%28CXN%29",
                    "verify",
                    new OptionDescriptor[] {OptionDescriptor.FILTER_MODE_PASS},
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.calculators.logp",
                    "LogP (CXN)",
                    "LogP using ChemAxon calculators",
                    new String[]{"logp", "partitioning", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogP+%28CXN%29",
                    "logp",
                    null, null),
            createServiceDescriptor(
                    "chemaxon.calculators.logd",
                    "LogD (CXN)",
                    "LogD at specified pH using ChemAxon calculators",
                    new String[]{"logd", "partitioning", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogD+%28CXN%29",
                    "logd",
                    createLogDOptionDescriptors(),
                    null),
            createServiceDescriptor(
                    "chemaxon.calculators.logs",
                    "LogS (CXN)",
                    "Solubility (logS) at specified pH using ChemAxon calculators",
                    new String[]{"logs", "solubility", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogS+%28CXN%29",
                    "logs",
                    createLogSOptionDescriptors(),
                    null),
            createServiceDescriptor(
                    "chemaxon.calculators.apka",
                    "Acidic pKa (CXN)",
                    "Most acidic pKa using ChemAxon calculators",
                    new String[]{"pka", "charge", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/pKa+%28CXN%29",
                    "apka",
                    null, null),
            createServiceDescriptor(
                    "chemaxon.calculators.bpka",
                    "Basic pKa (CXN)",
                    "Most basic pKa using ChemAxon calculators",
                    new String[]{"pka", "charge", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/pKa+%28CXN%29",
                    "bpka",
                    null, null),
            createServiceDescriptor(
                    "chemaxon.calculators.atomcount",
                    "Atom Count (CXN)",
                    "Atom Count using ChemAxon calculators",
                    new String[]{"atomcount", "topology", "molecularproperties", "chemaxon"},
                    "icons/properties_add.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Atom+Count+%28CXN%29",
                    "atomCount",
                    null, null),
            createServiceDescriptor(
                    "chemaxon.calculators.lipinski",
                    "Lipinski (CXN)",
                    "Lipinski rule of 5 filter using ChemAxon calculators",
                    new String[]{"lipinski", "filter", "druglike", "molecularproperties", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Lipinski+filter+%28CXN%29",
                    "lipinski",
                    createLipinskiOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.calculators.druglikefilter",
                    "Drug-like Filter (CXN)",
                    "Drug-like filter using ChemAxon calculators",
                    new String[]{"druglike", "filter", "molecularproperties", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Drug-like+Filter+%28CXN%29",
                    "drugLikeFilter",
                    createDrugLikeFilterOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.calculators.ghosefilter",
                    "Ghose Filter (CXN)",
                    "Ghose filter using ChemAxon calculators",
                    new String[]{"ghose", "filter", "druglike", "molecularproperties", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Ghose+filter+%28CXN%29",
                    "ghosefilter",
                    createGhoseFilterOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.calculators.veberfilter",
                    "Veber Filter (CXN)",
                    "Veber filter using ChemAxon calculators",
                    new String[]{"veber", "filter", "druglike", "psa", "tpsa", "rotatablebonds", "molecularproperties", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Veber+filter+%28CXN%29",
                    "veberfilter",
                    createVeberFilterOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.calculators.ruleofthreefilter",
                    "Rule of 3 Filter (CXN)",
                    "Astex Rule of 3 filter using ChemAxon calculators",
                    new String[]{"ruleofthree", "ro3", "hbond", "donors", "acceptors", "logp", "molecularweight", "rotatablebonds", "leadlike", "molecularproperties", "filter", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Rule+of+3+filter+%28CXN%29",
                    "ruleOfThreeFilter",
                    createRuleOfThreeOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.calculators.reosfilter",
                    "REOS (CXN)",
                    "Rapid Elimination Of Swill filter using ChemAxon calculators",
                    new String[]{"reos", "hbond", "donors", "acceptors", "logp", "molecularweight", "rotatablebonds", "charge", "formalcharge", "leadlike", "molecularproperties", "filter", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/REOS+filter+%28CXN%29",
                    "reosFilter",
                    createReosFilterOptionDescriptors(),
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.calculators.cnsMpo",
                    "CNS MPO (CXN)",
                    "CNS MPO score using ChemAxon calculators",
                    new String[]{"cns", "mpo", "hbond", "donors", "pka", "logp", "logd", "tpsa", "molecularweight", "molecularproperties", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/CNS+MPO+%28CXN%29",
                    "cnsMpo",
                    createMpoOptionDescriptors(),
                    null),
            createServiceDescriptor(
                    "chemaxon.calculators.kidsMpo",
                    "KiDS MPO (CXN)",
                    "KiDS MPO score using ChemAxon calculators",
                    new String[]{"kids", "mpo", "hbond", "donors", "tpsa", "rotatablebonds", "atomcount", "ringcount", "molecularproperties", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/KiDS+MPO+%28CXN%29",
                    "kidsMpo",
                    createMpoOptionDescriptors(),
                    null)

//                createServiceDescriptor(
//                        "chemaxon.calculators.chemterms",
//                        "CXN Chemical Terms",
//                        "Property prediction using a user definable Chemical Terms expression. See http://docs.chemaxon.com/display/chemicalterms/Chemical+Terms+Home",
//                        new String[]{"molecularproperties", "chemaxon"},
//                        new String[]{"/Vendors/ChemAxon/Calculators", "Chemistry/Calculators/General"},
//                        "asyncHttp",
//                        "chemTerms",
//                        0.01f,
//                        new ServicePropertyDescriptor[]{
//                            new ServicePropertyDescriptor(ServicePropertyDescriptor.Type.STRING, KEY_CT_EXPR, LABEL_CT_EXPR, DESC_CT_EXPR)
//                        })
    };

    static private OptionDescriptor[] createLogDOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(new OptionDescriptor<>(Float.class, "query.pH",
                "pH", "pH value", Mode.User).withMinMaxValues(1,1).withDefaultValue(7.4f));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createLogSOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(new OptionDescriptor<>(Float.class, "query.pH",
                "pH", "pH value", Mode.User).withMinMaxValues(1,1).withDefaultValue(7.4f));

// result type does not seem to be handled by this version of JChem
//        list.add(new OptionDescriptor<>(String.class, "query.result",
//                "Result type", "Result type")
//                .withMinMaxValues(1,1)
//                .withValues(new String[] {"logS", "mol/l", "mg/ml"})
//                .withDefaultValue("logS"));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createLipinskiOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(1)
                .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.MOLECULAR_WEIGHT,
                "Mol weight", "Molecular weight", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(0f, 500f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.LOGP,
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(null, 5.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HBOND_DONOR_COUNT,
                "HBD count", "H-bond donor count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 5)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT,
                "HBA count", "H-bond acceptor count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 10)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createDrugLikeFilterOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.MOLECULAR_WEIGHT,
                "Mol weight", "Molecular weight", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(0f, 400f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.RING_COUNT,
                "Ring count", "Ring count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(1, null)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.ROTATABLE_BOND_COUNT,
                "Rotatable bond count", "Rotatable bond count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 5)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HBOND_DONOR_COUNT,
                "HBD count", "H-bond donor count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 5)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT,
                "HBA count", "H-bond acceptor count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 10)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.LOGP,
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(null, 5.0f)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createGhoseFilterOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.LOGP,
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(-0.4f, 5.6f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.MOLECULAR_WEIGHT,
                "MolWeight", "molecular weight", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(160f, 480f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.ATOM_COUNT,
                "Atom count", "Atom count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(20, 70)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.MOLAR_REFRACTIVITY,
                "Refractivity", "Molar Refractivity", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(40f, 130f)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createVeberFilterOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.TPSA,
                "TPSA", "Topological polar surfacearea", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(0f, 140f)));

        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.ROTATABLE_BOND_COUNT,
                "Rotatable bond count", "Rotatable bond count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 10)));


        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createRuleOfThreeOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.LOGP,
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(null, 3.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.MOLECULAR_WEIGHT,
                "Mol weight", "Molecular weight", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(0f, 300f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HBOND_DONOR_COUNT,
                "HBD count", "H-bond donor count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 3)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT,
                "HBA count", "H-bond acceptor count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 3)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.ROTATABLE_BOND_COUNT,
                "Rot bond count", "Rotatable bond count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 3)));

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createReosFilterOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_PASS);
        list.add(new OptionDescriptor<>(Integer.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Number of violations", "Number of violations to accept", Mode.User)
                .withDefaultValue(0)
                .withMinMaxValues(1,1));

        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.MOLECULAR_WEIGHT,
                "Mol weight", "Molecular weight", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(200f, 500f)));
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + ChemTermsEvaluator.LOGP,
                "LogP", "LogP partition coefficient", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(-5.0f, 5.0f)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HBOND_DONOR_COUNT,
                "HBD count", "H-bond donor count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 5)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT,
                "HBA count", "H-bond acceptor count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 10)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.FORMAL_CHARGE,
                "Formal charge", "Formal charge", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(-2, 2)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.ROTATABLE_BOND_COUNT,
                "Rot bond count", "Rotatable bond count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(0, 8)));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + ChemTermsEvaluator.HEAVY_ATOM_COUNT,
                "Heavy atom count", "Heavy atom count", Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Integer(15, 50)));

        return list.toArray(new OptionDescriptor[0]);
    }


    static private OptionDescriptor[] createMpoOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(OptionDescriptor.FILTER_MODE_ALL);
        list.add(new OptionDescriptor<>(NumberRange.Float.class, "query." + CommonConstants.OPTION_FILTER_THRESHOLD, "Filter range",
                "Range of MPO scores to accept", Mode.User));

        return list.toArray(new OptionDescriptor[0]);
    }


    private static final HttpServiceDescriptor[] SERVICE_DESCRIPTOR_DESCRIPTORS
            = new HttpServiceDescriptor[]{
            createServiceDescriptor(
                    "chemaxon.screening.ecpf4",
                    "ECFP4 Screen (CXN)",
                    "Virtual screening using ChemAxon ECFP4 fingerprints. See http://www.chemaxon.com/products/screen/",
                    new String[]{"virtualscreening", "screening", "ecfp", "ecfp4", "moleculardescriptors", "fingerprints", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/ECFP4+Screen+%28CXN%29",
                    "screening/ecfp4",
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.DISCRETE, new String[]{"smiles"}), KEY_QMOL, LABEL_QMOL, DESC_QMOL, Mode.User),
                            new OptionDescriptor<>(Float.class, KEY_SIM_CUTTOFF, LABEL_SIM_CUTTOFF, DESC_SIM_CUTTOFF, Mode.User).withDefaultValue(0.7f)
                    },
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.screening.pharmacophore",
                    "Pharmacophore Screen (CXN)",
                    "Virtual screening using ChemAxon 2D pharmacophore fingerprints. See http://www.chemaxon.com/products/screen/",
                    new String[]{"virtualscreening", "screening", "parmacophore", "moleculardescriptors", "fingerprints", "chemaxon"},
                    "icons/filter_molecules.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Pharmacophore+Screen+%28CXN%29",
                    "screening/pharmacophore",
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.DISCRETE, new String[]{"smiles"}), KEY_QMOL, LABEL_QMOL, DESC_QMOL, Mode.User),
                            new OptionDescriptor<>(Float.class, KEY_SIM_CUTTOFF, LABEL_SIM_CUTTOFF, DESC_SIM_CUTTOFF, Mode.User).withDefaultValue(0.7f)
                    },
                    ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR),
            createServiceDescriptor(
                    "chemaxon.clustering.sperex",
                    "SpereEx Clustering (CXN)",
                    "Sphere exclusion clustering using ChemAxon ECFP4 fingerprints. See http://www.chemaxon.com/products/jklustor/",
                    new String[]{"clustering", "ecfp", "ecfp4", "chemaxon"},
                    "icons/clustering.png",
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/SphereEx+Clustering+%28CXN%29",
                    "clustering/spherex/ecfp4",
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(Integer.class, KEY_MIN_CLUSTERS, LABEL_MIN_CLUSTERS, DESC_MIN_CLUSTERS, Mode.User).withDefaultValue(5),
                            new OptionDescriptor<>(Integer.class, KEY_MAX_CLUSTERS, LABEL_MAX_CLUSTERS, DESC_MAX_CLUSTERS, Mode.User).withDefaultValue(10)
                    },
                    null)
    };

    static HttpServiceDescriptor createServiceDescriptor(String serviceDescriptorId, String name, String desc, String[] tags, String icon,
                                                         String resourceUrl, String endpoint, OptionDescriptor[] options, ThinDescriptor thinDescriptor) {
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
                .post("logd").description("Calculate the logD at specified pH for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_LOGD, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("logs").description("Calculate the solubility (LogS) at specified pH for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_LOGS, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("apka").description("Calculate the most acidic pKa value for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_APKA, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("bpka").description("Calculate the most basic pKa value for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_BPKA, resolver, ROUTE_STATS))
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
                .post("ghoseFilter").description("Apply a Ghose filter to the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_GHOSE_FILTER, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("veberFilter").description("Apply a Veber filter to the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_VEBER_FILTER, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("ruleOfThreeFilter").description("Apply a Rule of 3 filter to the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_RULE_OF_THREE, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("reosFilter").description("Apply a REOS filter to the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_REOS, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("cnsMpo").description("Generate the CNS MPO score for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_CNS_MPO, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("kidsMpo").description("Generate the KiDS MPO score for the supplied MoleculeObjects")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonCalculatorsRouteBuilder.CHEMAXON_KIDS_MPO, resolver, ROUTE_STATS))
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
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonDescriptorsRouteBuilder.CHEMAXON_SCREENING_ECFP4, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("screening/pharmacophore").description("Screen using pharmacophore fingerprints")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonDescriptorsRouteBuilder.CHEMAXON_SCREENING_PHARMACOPHORE, resolver, ROUTE_STATS))
                .endRest()
                //
                .post("clustering/spherex/ecfp4").description("Sphere exclusion clustering using ECFP4 fingerprints")
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(ChemaxonDescriptorsRouteBuilder.CHEMAXON_CLUSTERING_SPHEREX_ECFP4, resolver, ROUTE_STATS))
                .endRest();

        rest("v1/reactor").description("Library enumeration using ChemAxon Reactor")
                .bindingMode(RestBindingMode.off)
                .consumes("application/json")
                .produces("application/json")
                .post("react").description("Simple enumeration")
                .route()
                .process(new JsonToDatasetProcessor(MoleculeObject.class))
                .process(new ReactorProcessor("/chemaxon_reaction_library.zip", ROUTE_STATS))
                .process(new DatasetToJsonProcessor(MoleculeObject.class))
                .endRest();

    }

}
