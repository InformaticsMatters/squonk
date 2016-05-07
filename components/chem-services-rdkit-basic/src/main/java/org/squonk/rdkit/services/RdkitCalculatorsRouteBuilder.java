package org.squonk.rdkit.services;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.rdkit.RDKitMoleculeProcessor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.rdkit.io.RDKitMoleculeIOUtils;
import org.squonk.rdkit.mol.EvaluatorDefintion;

import java.util.stream.Stream;

import static org.squonk.rdkit.mol.EvaluatorDefintion.Function.NUM_ROTATABLE_BONDS;

/**
 * Basic services based on RDKit
 *
 * @author timbo
 */
public class RdkitCalculatorsRouteBuilder extends RouteBuilder {

    static final String RDKIT_LOGP = "direct:rdk_logp";
    static final String RDKIT_FRACTION_C_SP3 = "direct:rdk_fraction_c_sp3";
    static final String RDKIT_LIPINSKI = "direct:rdk_lipinski";
    static final String  RDKIT_DONORS_ACCEPTORS = "direct:rdk_donors_acceptors";
    static final String RDKIT_MOLAR_REFRACTIVITY = "direct:rdk_molar_refractivity";
    static final String RDKIT_TPSA = "direct:rdk_tpsa";
    static final String RDKIT_RINGS = "direct:rdk_rings";
    static final String RDKIT_ROTATABLE_BONDS = "direct:rdk_rotatable_bonds";
    static final String RDKIT_CANONICAL_SMILES = "direct:canonical_smiles";

    @Override
    public void configure() throws Exception {

        from(RDKIT_LOGP)
                .log("RDKIT_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefintion.Function.LOGP))
                .log("RDKIT_LOGP finished");

        from(RDKIT_FRACTION_C_SP3)
                .log("RDKIT_FRACTION_C_SP3 starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefintion.Function.FRACTION_C_SP3))
                .log("RDKIT_FRACTION_C_SP3 finished");

        from(RDKIT_LIPINSKI)
                .log("RDKIT_LIPINSKI starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor()
                        .calculate(EvaluatorDefintion.Function.LIPINSKI_HBA)
                        .calculate(EvaluatorDefintion.Function.LIPINSKI_HBD)
                        .calculate(EvaluatorDefintion.Function.LOGP)
                        .calculate(EvaluatorDefintion.Function.EXACT_MW)
                )
                .log("RDKIT_LIPINSKI finished");

        from(RDKIT_DONORS_ACCEPTORS)
                .log("RDKIT_DONORS_ACCEPTORS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor()
                        .calculate(EvaluatorDefintion.Function.NUM_HBD)
                        .calculate(EvaluatorDefintion.Function.NUM_HBA)
                )
                .log("RDKIT_DONORS_ACCEPTORS finished");

        from(RDKIT_MOLAR_REFRACTIVITY)
                .log("RDKIT_MOLAR_REFRACTIVITY starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefintion.Function.MOLAR_REFRACTIVITY))
                .log("RDKIT_MOLAR_REFRACTIVITY finished");

        from(RDKIT_TPSA)
                .log("RDKIT_TPSA starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(EvaluatorDefintion.Function.TPSA))
                .log("RDKIT_TPSA finished");

        from(RDKIT_RINGS)
                .log("RDKIT_NUM_RINGS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor()
                        .calculate(EvaluatorDefintion.Function.NUM_RINGS)
                        .calculate(EvaluatorDefintion.Function.NUM_AROMATIC_RINGS)
                )
                .log("RDKIT_NUM_RINGS finished");

        from(RDKIT_ROTATABLE_BONDS)
                .log("RDKIT_ROTATABLE_BONDS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate(NUM_ROTATABLE_BONDS))
                .log("RDKIT_ROTATABLE_BONDS finished");

        from(RDKIT_CANONICAL_SMILES)
                .log("RDKIT_CANONICAL_SMILES starting")
                .process((Exchange exch) -> {

                    Dataset<MoleculeObject> dataset = exch.getIn().getBody(Dataset.class);
                    if (dataset == null || dataset.getType() != MoleculeObject.class) {
                        throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
                    }

                    String modeS = exch.getIn().getHeader("mode", String.class);
                    final RDKitMoleculeIOUtils.FragmentMode mode = (modeS == null ? RDKitMoleculeIOUtils.FragmentMode.WHOLE_MOLECULE : RDKitMoleculeIOUtils.FragmentMode.valueOf(modeS.toUpperCase()));

                    Stream<MoleculeObject> results = dataset.getStream().peek((mo) -> {
                        String smiles = RDKitMoleculeIOUtils.generateCanonicalSmiles(mo, mode);
                        if (smiles != null) {
                            mo.putValue("CanSmiles_RDKit", smiles);
                        }
                    });

                    DatasetMetadata<MoleculeObject> meta = dataset.getMetadata();
                    if (meta == null) {
                        meta = new DatasetMetadata(MoleculeObject.class);
                    }
                    meta.getValueClassMappings().put("CanSmiles_RDKit", String.class);
                    exch.getIn().setHeader(CamelCommonConstants.HEADER_METADATA, meta);
                    exch.getIn().setBody(new MoleculeObjectDataset(results));
                })
                .log("RDKIT_CANONICAL_SMILES finished");

    }
}
