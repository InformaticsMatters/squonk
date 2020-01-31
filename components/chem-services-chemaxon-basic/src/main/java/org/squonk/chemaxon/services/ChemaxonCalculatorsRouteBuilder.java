/*
 * Copyright (c) 2020 Informatics Matters Ltd.
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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.chemaxon.processor.ChemAxonMoleculeProcessor;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.chemaxon.processor.ChemAxonVerifyStructureProcessor;
import org.squonk.camel.chemaxon.processor.calculations.BBBGuptaMPSProcessor;
import org.squonk.camel.processor.AbstractCalculationProcessor;
import org.squonk.camel.processor.PropertyFilterProcessor;
import org.squonk.camel.processor.MpoAccumulatorProcessor;
import org.squonk.chemaxon.molecule.BBBGuptaMPSCalculator;
import org.squonk.chemaxon.molecule.ChemTermsEvaluator;
import org.squonk.chemaxon.molecule.LazyPKaChemTermsEvaluator;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.util.CommonConstants;
import org.squonk.util.Metrics;
import org.squonk.util.MpoFunctions;
import org.squonk.util.StatsRecorder;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * These are routes that provide basic property calculation services. The input to the route is a
 * Stream&lt;MoleculeObject&gt; or better, a StreamProvider&lt;MoleculeObject&gt;, or anything that
 * can be converted to these by the Camel TypeConvertor mechanism. The output is a
 * StreamProvider&lt;MoleculeObject&gt; with the (possibly filtered) molecules having the
 * corresponding properties added.
 *
 * @author timbo
 */
public class ChemaxonCalculatorsRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(ChemaxonCalculatorsRouteBuilder.class.getName());

    public static final String CHEMAXON_STRUCTURE_VERIFY = "direct:structure_verify";
    public static final String CHEMAXON_LOGP = "direct:logp";
    public static final String CHEMAXON_LOGD = "direct:logd";
    public static final String CHEMAXON_LOGS = "direct:logs";
    public static final String CHEMAXON_APKA = "direct:apka";
    public static final String CHEMAXON_BPKA = "direct:bpka";
    public static final String CHEMAXON_ATOM_COUNT = "direct:atomcount";
    public static final String CHEMAXON_ATOM_BOND_COUNT = "direct:atomcount_bondcount";
    public static final String CHEMAXON_LIPINSKI = "direct:lipinski";
    public static final String CHEMAXON_DRUG_LIKE_FILTER = "direct:drug_like_filter";
    public static final String CHEMAXON_GHOSE_FILTER = "direct:ghose_filter";
    public static final String CHEMAXON_VEBER_FILTER = "direct:veber_filter";
    public static final String CHEMAXON_CHEMTERMS = "direct:chemterms";
    public static final String CHEMAXON_AROMATIZE = "direct:aromatize";
    public static final String CHEMAXON_RULE_OF_THREE = "direct:rule_of_3_filter";
    public static final String CHEMAXON_REOS = "direct:reos_filter";
    public static final String CHEMAXON_CNS_MPO = "direct:cns_mpo_score";
    public static final String CHEMAXON_KIDS_MPO = "direct:kids_mpo_score";
    public static final String CHEMAXON_ABBVIE_MPS = "direct:abbvie_mps_score";
    public static final String CHEMAXON_BBB_GUPTA_MPS = "direct:bbb_gupta_mps_score";


    @Override
    public void configure() throws Exception {

        from(CHEMAXON_STRUCTURE_VERIFY)
                .log("CHEMAXON_STRUCTURE_VERIFY starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonVerifyStructureProcessor())
                .log("CHEMAXON_STRUCTURE_VERIFY finished");


        // Simple route that calculates the LogP of molecules
        from(CHEMAXON_LOGP)
                .log("CHEMAXON_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .logP())
                .log("CHEMAXON_LOGP finished");

        // Calculates the LogD at a particular pH
        from(CHEMAXON_LOGD)
                .log("CHEMAXON_LOGD starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process((Exchange exch) -> {
                    Float pH = exch.getIn().getHeader("pH", Float.class);
                    Processor p = new ChemAxonMoleculeProcessor().logD(pH);
                    p.process(exch);
                })
                .log("CHEMAXON_LOGD finished");

        // Calculates the LogD at a particular pH
        from(CHEMAXON_LOGS)
                .log("CHEMAXON_LOGS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process((Exchange exch) -> {
                    Float pH = exch.getIn().getHeader("pH", Float.class);
                    String result = exch.getIn().getHeader("result", String.class);
                    Processor p = new ChemAxonMoleculeProcessor().logS(pH, result);
                    p.process(exch);
                })
                .log("CHEMAXON_LOGS finished");


        // Calculate most acidic pKa
        from(CHEMAXON_APKA)
                .log("CHEMAXON_APKA starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .apKa()
                )
                .log("CHEMAXON_APKA finished");

        // Calculate most basic pKa
        from(CHEMAXON_BPKA)
                .log("CHEMAXON_BPKA starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .bpKa()
                )
                .log("CHEMAXON_BPKA finished");

        // Calculate atom count
        from(CHEMAXON_ATOM_COUNT)
                .log("CHEMAXON_ATOM_COUNT starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .atomCount()
                )
                .log("CHEMAXON_ATOM_COUNT finished");

        // Calculate LogP, atom count and bond count
        from(CHEMAXON_ATOM_BOND_COUNT)
                .log("CHEMAXON_ATOM_BOND_COUNT starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .atomCount()
                        .bondCount()
                )
                .log("CHEMAXON_ATOM_BOND_COUNT finished");

        //Calculate the standard Lipinski properties
        from(CHEMAXON_LIPINSKI)
                .log("CHEMAXON_LIPINSKI starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .molWeight()
                        .logP()
                        .donorCount()
                        .acceptorCount()
                )
                .process(new PropertyFilterProcessor("Lipinski_FAILS_CXN") // filter
                        .filterInteger(ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT)
                        .filterInteger(ChemTermsEvaluator.HBOND_DONOR_COUNT)
                        .filterDouble(ChemTermsEvaluator.LOGP)
                        .filterDouble(ChemTermsEvaluator.MOLECULAR_WEIGHT)
                )
                .log("CHEMAXON_LIPINSKI finished");


        from(CHEMAXON_DRUG_LIKE_FILTER)
                .log("CHEMAXON_DRUG_LIKE_FILTER starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .molWeight()
                        .ringCount()
                        .rotatableBondCount()
                        .donorCount()
                        .acceptorCount()
                        .logP()
                ).process(new PropertyFilterProcessor("Lipinski_FAILS_CXN") // filter
                .filterDouble(ChemTermsEvaluator.MOLECULAR_WEIGHT)
                .filterInteger(ChemTermsEvaluator.RING_COUNT)
                .filterInteger(ChemTermsEvaluator.ROTATABLE_BOND_COUNT)
                .filterInteger(ChemTermsEvaluator.HBOND_DONOR_COUNT)
                .filterInteger(ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT)
                .filterDouble(ChemTermsEvaluator.LOGP))
                .log("CHEMAXON_DRUG_LIKE_FILTER finished");

        from(CHEMAXON_GHOSE_FILTER)
                .log("CHEMAXON_GHOSE_FILTER starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .logP()
                        .molWeight()
                        .atomCount()
                        .molarRefractivity()
                ).process(new PropertyFilterProcessor("Ghose_FAILS_CXN")
                .filterDouble(ChemTermsEvaluator.LOGP)
                .filterDouble(ChemTermsEvaluator.MOLECULAR_WEIGHT)
                .filterInteger(ChemTermsEvaluator.ATOM_COUNT)
                .filterDouble(ChemTermsEvaluator.MOLAR_REFRACTIVITY)
        ).log("CHEMAXON_GHOSE_FILTER finished");

        from(CHEMAXON_VEBER_FILTER)
                .log("CHEMAXON_VEBER_FILTER starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .rotatableBondCount()
                        .tpsa()

                ).process(new PropertyFilterProcessor("Veber_FAILS_CXN")
                .filterInteger(ChemTermsEvaluator.ROTATABLE_BOND_COUNT)
                .filterDouble(ChemTermsEvaluator.TPSA)
        ).log("CHEMAXON_VEBER_FILTER finished");

        from(CHEMAXON_RULE_OF_THREE)
                .log("CHEMAXON_RULE_OF_THREE starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor() // calculate
                        .logP()
                        .molWeight()
                        .acceptorCount()
                        .donorCount()
                        .rotatableBondCount()

                )
                .process(new PropertyFilterProcessor("RO3_FAILS_CDK") // filter
                        .filterDouble(ChemTermsEvaluator.LOGP)
                        .filterDouble(ChemTermsEvaluator.MOLECULAR_WEIGHT)
                        .filterInteger(ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT)
                        .filterInteger(ChemTermsEvaluator.HBOND_DONOR_COUNT)
                        .filterInteger(ChemTermsEvaluator.ROTATABLE_BOND_COUNT)
                )
                .log("CHEMAXON_RULE_OF_THREE finished");

        from(CHEMAXON_REOS)
                .log("CHEMAXON_REOS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor() // calculate
                        .molWeight()
                        .logP()
                        .donorCount()
                        .acceptorCount()
                        .formalCharge()
                        .rotatableBondCount()
                        .heavyAtomCount()
                )
                .process(new PropertyFilterProcessor("REOS_FAILS_CXN") // filter
                        .filterDouble(ChemTermsEvaluator.MOLECULAR_WEIGHT)
                        .filterDouble(ChemTermsEvaluator.LOGP)
                        .filterInteger(ChemTermsEvaluator.HBOND_DONOR_COUNT)
                        .filterInteger(ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT)
                        .filterInteger(ChemTermsEvaluator.FORMAL_CHARGE)
                        .filterInteger(ChemTermsEvaluator.ROTATABLE_BOND_COUNT)
                        .filterInteger(ChemTermsEvaluator.HEAVY_ATOM_COUNT)
                )
                .log("CHEMAXON_REOS finished");

        from(CHEMAXON_CNS_MPO)
                .log("CHEMAXON_CNS_MPO starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .logP()
                        .logD(7.4f)
                        .molWeight()
                        .tpsa()
                        .donorCount()
                        .bpKa()
                )
                .process(new MpoAccumulatorProcessor(
                        "CNS_MPO_CXN",
                        "CNS MPO score using ChemAxon calculators",
                        Float.class,
                        CommonConstants.OPTION_FILTER_MODE,
                        CommonConstants.OPTION_FILTER_THRESHOLD)
                        .addHumpFunction(ChemTermsEvaluator.LOGP,
                                MpoFunctions.createRampFunction(1d, 0d, 3d, 5d))
                        .addHumpFunction(ChemTermsEvaluator.LOGD + "_7.4",
                                MpoFunctions.createRampFunction(1d, 0d, 2d, 4d))
                        .addHumpFunction(ChemTermsEvaluator.MOLECULAR_WEIGHT,
                                MpoFunctions.createRampFunction(1d, 0d, 360d, 500d))
                        .addHumpFunction(ChemTermsEvaluator.TPSA,
                                MpoFunctions.createHump1Function(0d, 1d, 0d, 20d, 40d, 90d, 120d))
                        .addHumpFunction(ChemTermsEvaluator.HBOND_DONOR_COUNT,
                                MpoFunctions.createRampFunction(1d, 0d, 0.5d, 3.5d))
                        .addHumpFunction(ChemTermsEvaluator.BPKA,
                                MpoFunctions.createRampFunction(1d, 0d, 8d, 10d))
                );


        from(CHEMAXON_KIDS_MPO)
                .log("Creating route CHEMAXON_KIDS_MPO")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .tpsa()
                        .rotatableBondCount()
                        .atomCount("7", "AtomCount_N_CXN")
                        .atomCount("8", "AtomCount_O_CXN")
                        .donorCount()
                        .aromaticRingCount()
                )
                .process(new MpoAccumulatorProcessor(
                        "KiDS_MPO_CXN",
                        "KiDS MPO score using ChemAxon calculators",
                        Float.class,
                        CommonConstants.OPTION_FILTER_MODE,
                        CommonConstants.OPTION_FILTER_THRESHOLD)
                        .addHumpFunction(ChemTermsEvaluator.TPSA,
                                MpoFunctions.createHump1Function(0d, 1d, 0d, 64.63d, 75.85d, 92.40d, 138.3d))
                        .addHumpFunction(ChemTermsEvaluator.ROTATABLE_BOND_COUNT,
                                MpoFunctions.createHump1Function(0.2d, 1d, 0d, 1d, 2d, 3d, 5d))
                        .addHumpFunction("AtomCount_N_CXN",
                                MpoFunctions.createHump2Function(0d, 1d, 0.2d, 0d, 2d, 4d, 5d, 6d, 8d, 9d))
                        .addHumpFunction("AtomCount_O_CXN",
                                MpoFunctions.createHump1Function(0.2d, 1d, 0d, 0d, 1d, 1d, 3d))
                        .addHumpFunction(ChemTermsEvaluator.HBOND_DONOR_COUNT,
                                MpoFunctions.createHump2Function(0d, 1d, 0.2d, 0d, 0d, 2d, 3d, 4d, 6d, 7d))
                        .addHumpFunction(ChemTermsEvaluator.AROMATIC_RING_COUNT,
                                MpoFunctions.createHump2Function(0d, 1d, 0.2d, 0d, 1d, 3d, 3d, 4d, 4d, 5d))
                );


        from(CHEMAXON_ABBVIE_MPS)
                .log("Creating route CHEMAXON_ABBVIE_MPS")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .logD(7.4f)
                        .rotatableBondCount()
                        .aromaticRingCount()
                )
                .process(new AbstractCalculationProcessor(
                        "ABBVIE_MPS_CXN",
                        "AbbVie MPS score using ChemAxon calculators",
                        Float.class,
                        CommonConstants.OPTION_FILTER_MODE,
                        CommonConstants.OPTION_FILTER_THRESHOLD) {

                    /**
                     * Calculate abs(logD - 3) + num_aromatic_rings + num_rotatable_bonds
                     * @param mo
                     */
                    protected void processMoleculeObject(MoleculeObject mo) {
                        Integer nar = mo.getValue(ChemTermsEvaluator.AROMATIC_RING_COUNT, Integer.class);
                        Integer rotb = mo.getValue(ChemTermsEvaluator.ROTATABLE_BOND_COUNT, Integer.class);
                        Double logd = mo.getValue(ChemTermsEvaluator.LOGD + "_7.4", Double.class);
                        if (nar == null || rotb == null || logd == null) {
                            LOG.fine("Values not present");
                            // do nothing
                        } else {
                            double result = Math.abs(logd - 3) + (double)nar + (double)rotb;
                            mo.putValue(calculatedPropertyName, result);
                        }
                    }

                });


        from(CHEMAXON_BBB_GUPTA_MPS)
                .log("Creating route CHEMAXON_BBB_GUPTA_MPS")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .aromaticRingCount()
                        .heavyAtomCount()
                        .donorCount()
                        .acceptorCount()
                        .molWeight()
                        .tpsa()
                        .rotatableBondCount()
                        .apKa()
                        .bpKa()
                )
                .process(new BBBGuptaMPSProcessor(CommonConstants.OPTION_FILTER_MODE,
                        CommonConstants.OPTION_FILTER_THRESHOLD));

        // Dynamic route that requires the chem terms configuration to be set using the
        // ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION header property. 
        from(CHEMAXON_CHEMTERMS)
                .log("Creating route CHEMAXON_CHEMTERMS")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor());

        // Aromatize the molecules
        from(CHEMAXON_AROMATIZE)
                .log("Creating route CHEMAXON_AROMATIZE")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .standardize("aromatize")
                );

    }
}
