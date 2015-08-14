package com.im.squonk.rdkit.services;

import com.im.lac.camel.CamelCommonConstants;
import com.im.lac.camel.rdkit.RDKitMoleculeProcessor;
import org.apache.camel.builder.RouteBuilder;
import static com.im.lac.rdkit.mol.EvaluatorDefintion.Function.*;

/**
 * Basic services based on RDKit
 *
 * @author timbo
 */
public class RdkitCalculatorsRouteBuilder extends RouteBuilder {

    static final String RDKIT_LOGP = "direct:rdk_logp";
    static final String KEY_LOGP = "RDKit_LogP";
    static final String RDKIT_FRACTION_C_SP3 = "direct:rdk_fraction_c_sp3";
    static final String KEY_FRACTION_C_SP3 = "RDKit_FracCSP3";
    static final String RDKIT_LIPINSKI = "direct:rdk_lipinski";
    static final String KEY_LIPINSKI_HBA = "RDKit_Lip_HBA";
    static final String KEY_LIPINSKI_HBD = "RDKit_Lip_HBD";
    static final String KEY_LIPINSKI_LOGP = "RDKit_LogP";
    static final String KEY_LIPINSKI_MW = "RDKit_MW";
    static final String RDKIT_MOLAR_REFRACTIVITY = "direct:rdk_molar_refractivity";
    static final String RDKIT_TPSA = "direct:rdk_tpsa";
    static final String RDKIT_RINGS = "direct:rdk_rings";
    static final String RDKIT_ROTATABLE_BONDS = "direct:rdk_rotatable_bonds";

    @Override
    public void configure() throws Exception {

        from(RDKIT_LOGP)
                .log("RDKIT_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(KEY_LOGP, LOGP))
                .log("RDKIT_LOGP finished");

        from(RDKIT_FRACTION_C_SP3)
                .log("RDKIT_FRACTION_C_SP3 starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate("RDKit_FracCSP3", FRACTION_C_SP3))
                .log("RDKIT_FRACTION_C_SP3 finished");

        from(RDKIT_LIPINSKI)
                .log("RDKIT_LIPINSKI starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor()
                        .calculate(KEY_LIPINSKI_HBA, LIPINSKI_HBA)
                        .calculate(KEY_LIPINSKI_HBD, LIPINSKI_HBD)
                        .calculate(KEY_LIPINSKI_LOGP, LOGP)
                        .calculate(KEY_LIPINSKI_MW, EXACT_MW)
                )
                .log("RDKIT_LIPINSKI finished");

        from(RDKIT_MOLAR_REFRACTIVITY)
                .log("RDKIT_MOLAR_REFRACTIVITY starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate("RDKit_MolarRefractivity", MOLAR_REFRACTIVITY))
                .log("RDKIT_MOLAR_REFRACTIVITY finished");

        from(RDKIT_TPSA)
                .log("RDKIT_TPSA starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate("RDKit_TPSA", TPSA))
                .log("RDKIT_TPSA finished");

        from(RDKIT_RINGS)
                .log("RDKIT_NUM_RINGS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor()
                        .calculate("RDKit_RingCount", NUM_RINGS)
                        .calculate("RDKit_AromaticRingCount", NUM_AROMATIC_RINGS)
                )
                .log("RDKIT_NUM_RINGS finished");

        from(RDKIT_ROTATABLE_BONDS)
                .log("RDKIT_ROTATABLE_BONDS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate("RDKit_RotBondCount", NUM_ROTATABLE_BONDS))
                .log("RDKIT_ROTATABLE_BONDS finished");

    }
}
