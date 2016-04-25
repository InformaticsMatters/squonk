package org.squonk.rdkit.db.impl

import org.squonk.rdkit.db.FingerprintType
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable

/**
 * Created by timbo on 16/12/2015.
 */
class DrugbankTable extends RDKitTable {

    DrugbankTable(String schema, String baseTableName) {
        super(schema, baseTableName, MolSourceType.CTAB, [
                FingerprintType.RDKIT,
                FingerprintType.MORGAN_CONNECTIVITY_2,
                FingerprintType.MORGAN_FEATURE_2])
        addColumn("drugbank_id", "CHAR", "CHAR(7)")
        addColumn("drug_groups", "TEXT", "TEXT")
        addColumn("generic_name", "TEXT", "TEXT")
        addColumn("brands", "TEXT", "TEXT")
        addColumn("products", "TEXT", "TEXT")
    }

}
