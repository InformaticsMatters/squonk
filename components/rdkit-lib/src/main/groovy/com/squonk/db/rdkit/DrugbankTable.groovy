package com.squonk.db.rdkit

import com.squonk.db.dsl.RdkTable

/**
 * Created by timbo on 16/12/2015.
 */
class DrugbankTable extends RdkTable {

    DrugbankTable(String schema, String baseTableName, MolSourceType molSourceType, Collection<FingerprintType> fptypes) {
        super(schema, baseTableName, molSourceType, fptypes)
        addColumn("drugbank_id", "CHAR", "CHAR(7)")
        addColumn("drug_groups", "TEXT", "TEXT")
        addColumn("generic_name", "TEXT", "TEXT")
        addColumn("brands", "TEXT", "TEXT")
        addColumn("products", "TEXT", "TEXT")
    }

}
