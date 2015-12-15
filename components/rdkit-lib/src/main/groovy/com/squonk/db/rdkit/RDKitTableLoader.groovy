package com.squonk.db.rdkit

import com.im.lac.types.MoleculeObject
import groovy.sql.Sql
import groovy.util.logging.Log
import java.lang.reflect.Constructor
import java.sql.SQLException
import java.util.stream.Stream
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
@Log
class RDKitTableLoader extends RDKitTable {
    
   
    int batchSize = 100
    private final Map<String,Class> propertyToTypeMappings

    RDKitTableLoader(DataSource dataSource, String schema, String baseTable, MolSourceType molSourceType, Map<String,String> extraColumnDefs, Map<String,Class> propertyToTypeMappings) {
        super(dataSource, schema, baseTable, molSourceType, extraColumnDefs)
        this.propertyToTypeMappings = propertyToTypeMappings
    }
    
    void loadData(Stream<MoleculeObject> mols) {
        Sql db = getSql()
        try {
            executeBatch(db, mols)
        } finally {
            db.close()
            mols.close()
        }
    }
    
    void addColumn(String table, String colname, String coldef) {
        String sql1 = 'ALTER TABLE ' + table + ' DROP COLUMN IF EXISTS ' + colname + ' CASCADE'
        String sql2 = 'ALTER TABLE ' + table + ' ADD COLUMN ' + colname + ' ' + coldef
        log.info "SQL: $sql1"
        log.info "SQL: $sql2"
        Sql db = getSql()
        try {
            db.execute(sql1)
            db.execute(sql2)
            //log.info "Column $colname added"
        } finally {
            db.close()
        }
    } 
    
    void createMoleculesAndIndex() {
        String molfps = molfpsSchemaPlusTable()
        dropTable(molfps)

        String sql1 = 'SELECT * INTO ' + molfps + 
            ' FROM (SELECT id,mol_from_' + molSourceType.toString().toLowerCase() + '(structure::cstring) m  FROM ' +
        baseSchemaPlusTable() + ') tmp where m IS NOT NULL'
        String sql2 = 'ALTER TABLE ' + molfps + ' ADD PRIMARY KEY (id)'
        String sql3 = 'ALTER TABLE ' + molfps + ' ADD CONSTRAINT fk_' + getMolfpsTable() + '_id FOREIGN KEY (id) REFERENCES ' + baseSchemaPlusTable() + ' (id)'
        String sql4 = 'CREATE INDEX idx_' + getMolfpsTable() + '_m ON ' +  molfps + ' USING gist(m)'
        
        log.info "SQL: $sql1"
        log.info "SQL: $sql2"
        log.info "SQL: $sql3"
        log.info "SQL: $sql4"
        executeSql { db ->
            db.execute(sql1)
            db.execute(sql2)
            db.execute(sql3)
            db.execute(sql4)
        }
    }
    
    void addFpColumn(FingerprintType type) {
        String molfps = molfpsSchemaPlusTable()
        String col = type.colName
        addColumn(molfps, col, 'bfp')
        String sql1 = 'UPDATE ' + molfps + ' SET ' + col + ' = ' + String.format(type.function, 'm')
        String sql2 = 'CREATE INDEX idx_' + getMolfpsTable() + '_' + col + ' ON ' + molfps + ' USING gist(' + col + ')'
        
        log.info "SQL: $sql1"
        log.info "SQL: $sql2"
        try {
            executeSql { db ->
                db.execute(sql1)
                db.execute(sql2)
            }
        } catch (SQLException ex) {
            log.warn "ERROR: failed to create fingerprint index of type $type. Deleting fingerprint column $col."
            ex.printStackTrace()
            return
        }
        Metric.each {
            addSimSearchHelperFunction(type, it)
            addSimSearchHelperFunction(type, it)
        }
    }
    
    int testSSS() {
        String sql = 'SELECT count(*) FROM ' + molfpsSchemaPlusTable() + " WHERE m@>'c1cccc2c1nncc2'"
        log.info "SQL: $sql"
        return executeSql { db ->
            def count = db.firstRow(sql)[0]
            return count
        }
    }
    
    int testFpSearch(FingerprintType type, Metric metric) {
        String sql = 'SELECT count(*) FROM ' + molfpsSchemaPlusTable() + 
            ' WHERE ' + type.colName + metric.operator + 
        String.format(type.function,"'Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1'")
        log.info "SQL: $sql"
        return executeSql { db ->
            def count = db.firstRow(sql)[0]
            return count
        }
    }
    
    int getRowCount() {  
        Sql db = getSql()
        try {
            int rows = db.firstRow('select count(*) from ' + baseSchemaPlusTable())[0]
            log.info "Table $baseTable contains $rows rows"
            return rows
        } finally {
            db.close()
        }
    }
    
    void dropAllItems() {
        executeSql { db ->
            FingerprintType.each { fp ->
                Metric.each { m ->
                    String funcName = getSimSearchHelperFunctionName(fp, m)
                    String sql = 'DROP FUNCTION IF EXISTS ' + funcName + '(text)'
                    log.info "SQL: $sql"
                    db.execute(sql)
                }
            }
        }
        dropTable(molfpsSchemaPlusTable())
        dropTable(baseSchemaPlusTable())
    }
    
    private void dropTable(String name) {
        String sql = 'DROP TABLE IF EXISTS ' + name
        log.info "SQL: $sql"
        executeSql { db ->
            db.execute(sql)
        }
    }

    
    void createTables() {
        String sql = 'CREATE TABLE ' + baseSchemaPlusTable() + ' (\n' +
        '  id SERIAL NOT NULL PRIMARY KEY,\n' +
        '  structure TEXT,\n  ' +
        getExtraColumnDefintions(',\n  ') + '\n)'
        log.info "SQL: $sql"
        Sql db = getSql()
        try {
            db.execute(sql)
        } finally {
            db.close()
        }
    }
    
    private void executeBatch(Sql db, Stream<MoleculeObject> mols) {
        List values = []
        String qmarks = extraColumnDefs.collect { ',?' }.join('')
        String sql = 'INSERT INTO ' + baseSchemaPlusTable() + ' (structure,' + extraColumnDefs.keySet().join(',') + ') VALUES (?' + qmarks + ')'
        log.info "SQL: $sql"
        mols.eachWithIndex { m,i -> 
            values.clear()
            values << m.source
            propertyToTypeMappings.each { String k,Class cls->
                values << convert(m.getValue(k), cls)
            }
            
            db.withBatch(batchSize, sql) { ps ->
                ps.addBatch(values)
            }
            if (i % 10000 == 0 && i > 0) {
                log.info "  loaded $i records"
            }
        }

        log.info "Finished loading" 
    }
    
    private def convert(Object val, Class cls) {
        if (val == null) {
            return null
        } else if (cls.isInstance(val))  {
            return val
        } else {
            Constructor con = cls.getConstructor(String.class)
            // TODO also allow for constuctor of the specific type
            def o = con.newInstance(val.toString())
            return o
        }
    }
    
    private void addSimSearchHelperFunction(FingerprintType type, Metric metric) {
        String sql = 'CREATE OR REPLACE FUNCTION ' + getSimSearchHelperFunctionName(type, metric) + '(smiles text)' +
            '\n  RETURNS table(id INTEGER, structure TEXT, m MOL, similarity DOUBLE PRECISION' + extraColumnDefs.collect { ', ' + it.key + ' ' + it.value }.join('') + ') AS' +
            '\n$$\nSELECT m.id, b.structure, m.m, ' +
        String.format(metric.function, String.format(type.function, 'mol_from_smiles($1::cstring)') + ',' + type.colName) + ' AS similarity' +
        extraColumnDefs.keySet().collect { ', b.' + it }.join('') +
        '\n FROM ' +  molfpsSchemaPlusTable() + ' m JOIN ' + baseSchemaPlusTable() + ' b ON m.id = b.id\n WHERE ' + 
        String.format(type.function, 'mol_from_smiles($1::cstring)') + metric.operator + type.colName + 
        '\n ORDER BY ' + 
        String.format(type.function, 'mol_from_smiles($1::cstring)') + '<' + metric.operator + '>' + type.colName + ';' +
        '\n$$ LANGUAGE SQL STABLE'
  
        log.info "SQL: $sql"
        Sql db = getSql()
        try {
            db.execute(sql)
        } finally {
            db.close()
        }
    }
    
}

