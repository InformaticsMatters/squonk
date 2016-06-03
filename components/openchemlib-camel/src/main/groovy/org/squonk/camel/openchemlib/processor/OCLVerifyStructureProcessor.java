package org.squonk.camel.openchemlib.processor;

import com.im.lac.types.MoleculeObject;
import org.squonk.camel.processor.VerifyStructureProcessor;
import org.squonk.openchemlib.molecule.OCLMoleculeUtils;

/**
 * Created by timbo on 29/05/16.
 */
public class OCLVerifyStructureProcessor extends VerifyStructureProcessor {

    public OCLVerifyStructureProcessor() {
        super("ValidMol_OCL");
    }

    @Override
    protected boolean validateMolecule(MoleculeObject mo) {
        return OCLMoleculeUtils.fetchMolecule(mo, false) != null;
    }
}
