package org.squonk.rdkit.db;

/**
 * Created by timbo on 13/12/2015.
 */
public enum MolSourceType {

    CTAB("mol_from_ctab(%s)", "qmol_from_ctab(%s)"), SMILES("mol_from_smiles(%s)", "qmol_from_smiles(%s)"), SMARTS("mol_from_smarts(%s)", "mol_from_smarts(%s)");

    public String molFunction;
    public String qmolFunction;

    MolSourceType(String molFunction, String qmolFunction) {
        this.molFunction = molFunction;
        this.qmolFunction = qmolFunction;
    }
}
