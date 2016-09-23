package org.squonk.chemaxon.services;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.chemaxon.processor.ChemAxonMoleculeProcessor;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.chemaxon.processor.ChemAxonVerifyStructureProcessor;
import org.squonk.camel.processor.PropertyFilterProcessor;
import org.squonk.chemaxon.molecule.ChemTermsEvaluator;
import org.squonk.util.Metrics;

import static org.squonk.util.Metrics.*;

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
                .process((Exchange exch) ->  {
                    Float pH = exch.getIn().getHeader("pH", Float.class);
                    Processor p = new ChemAxonMoleculeProcessor().logD(pH);
                    p.process(exch);
                })
                .log("CHEMAXON_LOGD finished");

        // Calculates the LogD at a particular pH
        from(CHEMAXON_LOGS)
                .log("CHEMAXON_LOGS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process((Exchange exch) ->  {
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


        // Dynamic route that requires the chem terms configuration to be set using the
        // ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION header property. 
        from(CHEMAXON_CHEMTERMS)
                .log("CHEMAXON_CHEMTERMS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor())
                .log("CHEMAXON_CHEMTERMS finished");

        // Aromatize the molecules
        from(CHEMAXON_AROMATIZE)
                .log("CHEMAXON_AROMATIZE starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .standardize("aromatize")
                )
                .log("CHEMAXON_AROMATIZE finished");

    }
}
