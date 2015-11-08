package com.squonk.rdkit.db

import groovy.sql.Sql
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
abstract class RDKitTable {
    
    enum MolSourceType {
        CTAB, SMILES
    }
    
    protected final DataSource dataSource
    protected final String schema
    protected final String baseTable
    protected final MolSourceType molSourceType = MolSourceType.CTAB
    protected final Map<String,String> extraColumnDefs = [version_id:'INTEGER', parent_id:'INTEGER']
    
    RDKitTable(DataSource dataSource, String schema, String baseTable, MolSourceType molSourceType, Map<String,String> extraColumnDefs) {
        this.dataSource = dataSource
        this.schema = schema
        this.baseTable = baseTable
        this.molSourceType = molSourceType
        this.extraColumnDefs = extraColumnDefs
    }
    
    
    String getMolfpsTable() {
        return baseTable + "_molfps"
    }
    
    String baseSchemaPlusTable() {
        return schema + '.' + baseTable
    }
    
    String molfpsSchemaPlusTable() {
        return schema + '.' + getMolfpsTable()
    }
    
    protected Sql getSql() {
        return new Sql(dataSource.getConnection())
    }
    
    protected def executeSql(Closure cl) {
        Sql db = getSql()
        try {
            return cl(db)
        } finally {
            db.close()
        }
    }
	
}

