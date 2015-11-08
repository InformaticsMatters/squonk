package com.squonk.rdkit.db

import com.im.lac.types.MoleculeObject
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import java.util.stream.Stream
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
class RDKitTableSearch extends RDKitTable {
    
    RDKitTableSearch(DataSource dataSource, String schema, String baseTable, MolSourceType molSourceType, Map<String,String> extraColumnDefs) {
        super(dataSource, schema, baseTable, molSourceType, extraColumnDefs)
    }
    
    List<MoleculeObject> sss(String smarts, boolean chiral) {
        String sql1 = 'SET rdkit.do_chiral_sss=' + chiral
        String sql2 = 'SELECT b.* FROM ' +  baseSchemaPlusTable() + ' b JOIN ' + molfpsSchemaPlusTable() + ' m ON b.id = m.id WHERE m.m@>?::mol'
        println "SQL: $sql1"
        println "SQL: $sql2"
        
        return executeSql { db ->
            db.execute(sql1)
            List<MoleculeObject> mols = []
            db.eachRow(sql2, [smarts]) { row ->
                MoleculeObject mo = new MoleculeObject(row.structure, molSourceType == MolSourceType.CTAB ? 'mol' : 'smiles')
                extraColumnDefs.keySet().each { k ->
                    def o = row[k]
                    if (o != null) {
                        mo.putValue(k, o)
                    }
                }
                mols << mo
            }
            return mols
        }
    }
    
	
}

