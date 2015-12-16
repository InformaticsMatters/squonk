package com.squonk.db.rdkit

import com.im.lac.types.MoleculeObject
import com.squonk.db.dsl.DataSourceConfiguration
import com.squonk.db.dsl.RdkTable
import com.squonk.db.dsl.SqlQuery
import com.squonk.reader.SDFReader
import com.squonk.util.IOUtils

import javax.sql.DataSource
import java.util.stream.Stream

/**
 * Created by timbo on 16/12/2015.
 */
class EMoleculesTable extends RdkTable {

    EMoleculesTable(String schema, String baseTableName, MolSourceType molSourceType) {
        super(schema, baseTableName, molSourceType, [
                FingerprintType.RDKIT,
                FingerprintType.MORGAN_CONNECTIVITY_2,
                FingerprintType.MORGAN_FEATURE_2])
        addColumn("version_id", "INTEGER", "INTEGER NOT NULL")
        addColumn("parent_id", "INTEGER", "INTEGER NOT NULL")
    }
}
