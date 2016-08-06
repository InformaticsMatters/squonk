package org.squonk.rdkit.services;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.rdkit.processor.RDKitCanonicalSmilesGeneratorProcessor;
import org.squonk.camel.rdkit.processor.RDKitMoleculeProcessor;
import org.squonk.camel.processor.PropertyFilterProcessor;
import org.squonk.camel.rdkit.processor.RDKitVerifyStructureProcessor;
import org.squonk.rdkit.mol.EvaluatorDefinition;

import java.util.logging.Logger;

import static org.squonk.rdkit.mol.EvaluatorDefinition.Function.*;

/**
 * Basic services based on RDKit
 *
 * @author timbo
 */
public class RdkitCalculatorsRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitCalculatorsRouteBuilder.class.getName());

    static final String RDKIT_STRUCTURE_VERIFY = "direct:structure_verify";
    static final String RDKIT_LOGP = "direct:rdk_logp";
    static final String RDKIT_FRACTION_C_SP3 = "direct:rdk_fraction_c_sp3";
    static final String RDKIT_LIPINSKI = "direct:rdk_lipinski";
    static final String RDKIT_REOS = "direct:rdk_reos";
    static final String RDKIT_DONORS_ACCEPTORS = "direct:rdk_donors_acceptors";
    static final String RDKIT_MOLAR_REFRACTIVITY = "direct:rdk_molar_refractivity";
    static final String RDKIT_TPSA = "direct:rdk_tpsa";
    static final String RDKIT_RINGS = "direct:rdk_rings";
    static final String RDKIT_ROTATABLE_BONDS = "direct:rdk_rotatable_bonds";
    static final String RDKIT_CANONICAL_SMILES = "direct:canonical_smiles";
    static final String RDKIT_FORMAL_CHARGE = "direct:formal_charge";

    @Override
    public void configure() throws Exception {

        from(RDKIT_STRUCTURE_VERIFY)
                .log("RDKIT_STRUCTURE_VERIFY starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitVerifyStructureProcessor())
                .log("RDKIT_STRUCTURE_VERIFY finished");


        from(RDKIT_LOGP)
                .log("RDKIT_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefinition.Function.LOGP))
                .log("RDKIT_LOGP finished");

        from(RDKIT_FRACTION_C_SP3)
                .log("RDKIT_FRACTION_C_SP3 starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefinition.Function.FRACTION_C_SP3))
                .log("RDKIT_FRACTION_C_SP3 finished");

        from(RDKIT_LIPINSKI)
                .log("RDKIT_LIPINSKI starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor()
                        .calculate(EvaluatorDefinition.Function.LIPINSKI_HBA)
                        .calculate(EvaluatorDefinition.Function.LIPINSKI_HBD)
                        .calculate(EvaluatorDefinition.Function.LOGP)
                        .calculate(EvaluatorDefinition.Function.EXACT_MW)
                )
                .log("RDKIT_LIPINSKI finished");

        from(RDKIT_REOS)
                .log("RDKIT_REOS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor() // calculate
                        .calculate(EXACT_MW)
                        .calculate(LOGP)
                        .calculate(NUM_HBD)
                        .calculate(NUM_HBA)
                        .calculate(FORMAL_CHARGE)
                        .calculate(NUM_ROTATABLE_BONDS)
                        .calculate(HEAVY_ATOM_COUNT)
                )
                .process(new PropertyFilterProcessor("REOS_FAILS_RDKit") // filter
                        .filterDouble(EXACT_MW.getName())
                        .filterDouble(LOGP.getName())
                        .filterInteger(NUM_HBD.getName())
                        .filterInteger(NUM_HBA.getName())
                        .filterInteger(FORMAL_CHARGE.getName())
                        .filterInteger(NUM_ROTATABLE_BONDS.getName())
                        .filterInteger(HEAVY_ATOM_COUNT.getName())
                )
                .log("RDKIT_REOS finished");

        from(RDKIT_DONORS_ACCEPTORS)
                .log("RDKIT_DONORS_ACCEPTORS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor()
                        .calculate(EvaluatorDefinition.Function.NUM_HBD)
                        .calculate(EvaluatorDefinition.Function.NUM_HBA)
                )
                .log("RDKIT_DONORS_ACCEPTORS finished");

        from(RDKIT_MOLAR_REFRACTIVITY)
                .log("RDKIT_MOLAR_REFRACTIVITY starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefinition.Function.MOLAR_REFRACTIVITY))
                .log("RDKIT_MOLAR_REFRACTIVITY finished");

        from(RDKIT_TPSA)
                .log("RDKIT_TPSA starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefinition.Function.TPSA))
                .log("RDKIT_TPSA finished");

        from(RDKIT_RINGS)
                .log("RDKIT_NUM_RINGS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor()
                        .calculate(EvaluatorDefinition.Function.NUM_RINGS)
                        .calculate(EvaluatorDefinition.Function.NUM_AROMATIC_RINGS)
                )
                .log("RDKIT_NUM_RINGS finished");

        from(RDKIT_ROTATABLE_BONDS)
                .log("RDKIT_ROTATABLE_BONDS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(NUM_ROTATABLE_BONDS))
                .log("RDKIT_ROTATABLE_BONDS finished");

        from(RDKIT_CANONICAL_SMILES)
                .log("RDKIT_CANONICAL_SMILES starting")
                .process(new RDKitCanonicalSmilesGeneratorProcessor("CanSmiles_RDKit"))
                .log("RDKIT_CANONICAL_SMILES finished");

        from(RDKIT_FORMAL_CHARGE)
                .log("RDKIT_FORMAL_CHARGE starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefinition.Function.FORMAL_CHARGE))
                .log("RDKIT_FORMAL_CHARGE finished");

    }

}
