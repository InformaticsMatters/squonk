package org.squonk.db.rdkit

import com.im.lac.types.MoleculeObject
import org.squonk.db.rdkit.dsl.IConfiguration
import groovy.sql.Sql
import groovy.util.logging.Log

import java.lang.reflect.Constructor
import java.sql.SQLException
import java.util.stream.Stream

/**
 *
 * @author timbo
 */
@Log
class RDKitTableLoader {


    int batchSize = 100
    int reportingSize = 10000
    private final RDKitTable rdkTable
    private final IConfiguration config

    RDKitTableLoader(RDKitTable rdkTable, IConfiguration config) {
        this.rdkTable = rdkTable
        this.config = config
    }

    protected Sql getSql() {
        return new Sql(config.connection)
    }

    protected def executeSql(Closure cl) {
        Sql db = getSql()
        try {
            return cl(db)
        } finally {
            db.close()
        }
    }

    private baseSchemaPlusTable() {
        return rdkTable.schemaPlusTable()
    }

    private String molfpsSchemaPlusTable() {
        rdkTable.molFpTable.schemaPlusTable()
    }

    private String molfpsTable() {
        rdkTable.molFpTable.getBaseName()
    }

    void loadData(Stream<MoleculeObject> mols, Map<String, Class> propertyToTypeMappings) {
        Sql db = getSql()
        try {
            executeBatch(db, mols, propertyToTypeMappings)
        } finally {
            db.close()
            mols.close()
        }
    }

    private void addColumn(String table, String colname, String coldef) {
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
        String schemaTableName = molfpsSchemaPlusTable()
        String tableName = molfpsTable()
        dropTable(schemaTableName)

        String sql1 = 'SELECT * INTO ' + schemaTableName +
                ' FROM (SELECT id,mol_from_' + rdkTable.molSourceType.toString().toLowerCase() + '(structure::cstring) m  FROM ' +
                baseSchemaPlusTable() + ') tmp where m IS NOT NULL'
        String sql2 = 'ALTER TABLE ' + schemaTableName + ' ADD PRIMARY KEY (id)'
        String sql3 = 'ALTER TABLE ' + schemaTableName + ' ADD CONSTRAINT fk_' + tableName + '_id FOREIGN KEY (id) REFERENCES ' + baseSchemaPlusTable() + ' (id)'
        String sql4 = 'CREATE INDEX idx_' + tableName + '_m ON ' + schemaTableName + ' USING gist(m)'

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

    void addFpColumns() {
        rdkTable.fingerprintTypes.each {
            addFpColumn(it)
        }
    }


    void addFpColumn(FingerprintType type) {
        String molfps = molfpsSchemaPlusTable()
        String col = type.colName
        addColumn(molfps, col, 'bfp')
        String sql1 = 'UPDATE ' + molfps + ' SET ' + col + ' = ' + String.format(type.function, 'm')
        String sql2 = 'CREATE INDEX idx_' + molfpsTable() + '_' + col + ' ON ' + molfps + ' USING gist(' + col + ')'

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
                String.format(type.function, "'Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1'")
        log.info "SQL: $sql"
        return executeSql { db ->
            def count = db.firstRow(sql)[0]
            return count
        }
    }

    int getRowCount() {
        Sql db = getSql()
        try {
            String baseTable = baseSchemaPlusTable()
            int rows = db.firstRow('select count(*) from ' + baseTable)[0]
            log.info "Table $baseTable contains $rows rows"
            return rows
        } finally {
            db.close()
        }
    }

    void dropAllItems() {
        executeSql { db ->
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
                rdkTable.columns.collect {
                    "  " + it.name + " " + it.definition
                }.join(",\n") + '\n)'
        log.info "SQL: $sql"
        Sql db = getSql()
        try {
            db.execute(sql)
        } finally {
            db.close()
        }
    }

    private void executeBatch(Sql db, Stream<MoleculeObject> mols, Map<String, Class> propertyToTypeMappings) {
        List values = []
        String cols = rdkTable.columns[1..(rdkTable.columns.size() -1)].collect { it.name }.join(',')
        String qmarks = rdkTable.columns[1..(rdkTable.columns.size() -1)].collect { '?' }.join(',')
        String sql = 'INSERT INTO ' + baseSchemaPlusTable() + ' (' + cols + ') VALUES (' + qmarks + ')'
        log.info "SQL: $sql"
        mols.eachWithIndex { m, i ->
            values.clear()
            values << m.source
            propertyToTypeMappings.each { String k, Class cls ->
                values << convert(m.getValue(k), cls)
            }

            db.withBatch(batchSize, sql) { ps ->
                ps.addBatch(values)
            }
            if (i % reportingSize == 0 && i > 0) {
                log.info "  loaded $i records"
            }
        }

        log.info "Finished loading"
    }

    private def convert(Object val, Class cls) {
        if (val == null) {
            return null
        } else if (cls.isInstance(val)) {
            return val
        } else {
            Constructor con = cls.getConstructor(String.class)
            // TODO also allow for constuctor of the specific type
            def o = con.newInstance(val.toString())
            return o
        }
    }

}

