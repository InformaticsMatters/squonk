package org.squonk.camel.chemaxon.processor;

import org.squonk.types.MoleculeObject;
import org.squonk.camel.processor.VerifyStructureProcessor;
import org.squonk.chemaxon.molecule.MoleculeUtils;

/**
 * Created by timbo on 29/05/16.
 */
public class ChemAxonVerifyStructureProcessor extends VerifyStructureProcessor {

    public ChemAxonVerifyStructureProcessor() {
        super("ValidMol_CXN");
    }

    @Override
    protected boolean validateMolecule(MoleculeObject mo) {
        return MoleculeUtils.fetchMolecule(mo, false) != null;
    }
}
