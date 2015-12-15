package com.squonk.db

/**
 * Created by timbo on 08/12/2015.
 */
class BaseRDKitTable extends DbTable {

    final DbColumn ID
    final DbColumn STRUCTURE

    BaseRDKitTable(String schema, String name) {
        super(schema, name)
        ID = addColumn(this, "id", "SERIAL")
        STRUCTURE = addColumn(this, "structure", "TEXT")
        setPrimaryKey(ID)
    }

}
