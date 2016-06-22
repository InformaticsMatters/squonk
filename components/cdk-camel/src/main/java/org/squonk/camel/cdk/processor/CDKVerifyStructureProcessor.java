package org.squonk.camel.cdk.processor;

import org.squonk.types.MoleculeObject;
import org.squonk.camel.processor.VerifyStructureProcessor;
import org.squonk.cdk.io.CDKMoleculeIOUtils;

/**
 * Created by timbo on 29/05/16.
 */
public class CDKVerifyStructureProcessor extends VerifyStructureProcessor {

    public CDKVerifyStructureProcessor() {
        super("ValidMol_CDK");
    }

    @Override
    protected boolean validateMolecule(MoleculeObject mo) {
        return CDKMoleculeIOUtils.fetchMolecule(mo, false) != null;
    }
}
