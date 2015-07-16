package com.im.lac.services.chemaxon;

import com.im.lac.camel.CamelCommonConstants;
import com.im.lac.camel.chemaxon.processor.ChemAxonMoleculeProcessor;

import org.apache.camel.builder.RouteBuilder;

/**
 * These are routes that provide basic property calculation services. The input to the route is a
 * Stream&lt;MoleculeObject&gt; or better, a StreamProvider&lt;MoleculeObject&gt;, or anything that
 * can be converted to these by the Camel TypeConvertor mechanism. The output is a
 * StreamProvider&lt;MoleculeObject&gt; with the (possibly filtered) molecules having the
 * corresponding properties added.
 *
 *
 * @author timbo
 */
public class CalculatorsRouteBuilder extends RouteBuilder {

    public static final String CHEMAXON_LOGP = "direct:logp";
    public static final String CHEMAXON_ATOM_COUNT = "direct:atomcount";
    public static final String CHEMAXON_ATOM_BOND_COUNT = "direct:atomcount_bondcount";
    public static final String CHEMAXON_LIPINSKI = "direct:lipinski";
    public static final String CHEMAXON_DRUG_LIKE_FILTER = "direct:drug_like_filter";
    public static final String CHEMAXON_CHEMTERMS = "direct:chemterms";
    public static final String CHEMAXON_AROMATIZE = "direct:aromatize";

    @Override
    public void configure() throws Exception {

        // Simple route that calculates the LogP of molecules
        from(CHEMAXON_LOGP)
                .log("CHEMAXON_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("CXN_LogP", "logP()"))
                .log("CHEMAXON_LOGP finished");

        // Calculate atom count
        from(CHEMAXON_ATOM_COUNT)
                .log("CHEMAXON_ATOM_COUNT starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("atom_count", "atomCount()"))
                .log("CHEMAXON_ATOM_COUNT finished");

        // Calculate LogP, atom count and bond count
        from(CHEMAXON_ATOM_BOND_COUNT)
                .log("CHEMAXON_ATOM_BOND_COUNT starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("atom_count", "atomCount()")
                        .calculate("bond_count", "bondCount()")
                )
                .log("CHEMAXON_ATOM_BOND_COUNT finished");

        //Calculate the standard Lipinski properties
        from(CHEMAXON_LIPINSKI)
                .log("CHEMAXON_LIPINSKI starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("mol_weight", "mass()")
                        .calculate("logp", "logP()")
                        .calculate("hbd_count", "donorCount()")
                        .calculate("hba_count", "acceptorCount()")
                )
                .log("CHEMAXON_LIPINSKI finished");

        // Simple route that exemplifies filtering using a basic drug-like filter
        from(CHEMAXON_DRUG_LIKE_FILTER)
                .log("CHEMAXON_DRUG_LIKE_FILTER starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .filter("mass()<400")
                        .filter("ringCount()>0")
                        .filter("rotatableBondCount()<5")
                        .filter("donorCount()<=5")
                        .filter("acceptorCount()<=10")
                        .filter("logP()<5")
                )
                .log("CHEMAXON_DRUG_LIKE_FILTER finished");

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
