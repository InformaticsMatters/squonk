package org.squonk.rdkit.db.impl

import org.squonk.rdkit.db.FingerprintType
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable

/**
 * Created by timbo on 16/12/2015.
 */
class PdbLigandTable extends RDKitTable {

    PdbLigandTable(String schema, String baseTableName) {
        super(schema, baseTableName, MolSourceType.CTAB, [
                FingerprintType.RDKIT,
                FingerprintType.MORGAN_CONNECTIVITY_2,
                FingerprintType.MORGAN_FEATURE_2])
        addColumn("pdb_code", "CHAR", "CHAR(4)")
        addColumn("ligand_code", "TEXT", "TEXT")

    }

}
