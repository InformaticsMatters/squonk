package org.squonk.camel.rdkit.processor;

import org.squonk.types.MoleculeObject;
import org.squonk.camel.processor.VerifyStructureProcessor;
import org.squonk.rdkit.mol.MolReader;

/**
 * Created by timbo on 29/05/16.
 */
public class RDKitVerifyStructureProcessor extends VerifyStructureProcessor {

    public RDKitVerifyStructureProcessor() {
        super("ValidMol_RDKit");
    }

    @Override
    protected boolean validateMolecule(MoleculeObject mo) {
        return MolReader.findROMol(mo, false) != null;
    }
}
