import groovy.sql.Sql
import java.sql.Connection
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
class EMoleculesETL extends AbstractETL {
    
    ConfigObject emolecules
    String emoleculesTable, concordanceTable
    
    int limit, offset
    int fetchSize = 500

    
    static void main(String[] args) {
        println "Running with $args"
        def instance = new EMoleculesETL()
        instance.run()
    }
    
    EMoleculesETL() {
        emolecules = Utils.createConfig('emolecules.properties')
        
        this.emoleculesTable = database.vendordbs.schema + '.' + emolecules.table
        this.concordanceTable = database.chemcentral.schema + '.' + emolecules.table + '_concordance'
                
        this.offset = emolecules.offset
        this.limit = emolecules.limit
        
        generateSqls()
    }
    
    void generateSqls() {
        createConcordanceTableSql = """\
            |CREATE TABLE $concordanceTable (
            |  structure_id INTEGER NOT NULL,
            |  cd_id INTEGER NOT NULL,
            |  CONSTRAINT fk_con2stuctures FOREIGN KEY (structure_id) references $chemcentralStructureTable (cd_id) ON DELETE CASCADE,
            |  CONSTRAINT fk_con2cdid FOREIGN KEY (cd_id) references $emoleculesTable (cd_id) ON DELETE CASCADE
            |)""".stripMargin()
        
        readStructuresSql = "SELECT cd_id, cd_structure FROM $emoleculesTable".toString()
        limit && (readStructuresSql += " LIMIT $limit")
        offset && (readStructuresSql += " OFFSET $offset")
        
        insertConcordanceSql = "INSERT INTO $concordanceTable (structure_id, cd_id) VALUES (?, ?)".toString()
        
        countConcordanceSql = "SELECT count(*) FROM $concordanceTable".toString()
        
        createStructureIdIndexSql = "CREATE INDEX idx_con_emol${emolecules.section}_structure_id on $concordanceTable (structure_id)"
        createDBCdidIndexSql = "CREATE INDEX idx_con_emol${emolecules.section}_cd_id on $concordanceTable (cd_id)"
        
        deleteAliasesSql = "DELETE FROM $chemcentralStructureAliasesTable WHERE source_id = ?"
        
        deleteSourceSql = "DELETE FROM $chemcentralSourcesTable WHERE source_name = ?"
        
        insertAliasesSql = """\
            |INSERT INTO $chemcentralStructureAliasesTable (structure_id, source_id, alias_value)
            |  SELECT con.structure_id, ?, e.version_id
            |    FROM $concordanceTable con
            |    JOIN $emoleculesTable e ON e.cd_id = con.cd_id""".stripMargin()
        
        insertPropertyDefinitionsSql = """\
                |INSERT INTO $chemcentralPropertyDefintionsTable (source_id, property_description, est_size)
                |  VALUES (?, 'eMolecules building blocks record', ?)""".stripMargin()
        
        insertStructurePropsSql = """\
                |INSERT INTO $chemcentralStructurePropertiesTable
                |  (structure_id, property_def_id, property_data)
                |  SELECT con.structure_id, ?, row_to_json(e)::jsonb
                |    FROM (SELECT cd_id, version_id, parent_id FROM $emoleculesTable) e
                |    JOIN $concordanceTable con ON con.cd_id = e.cd_id""".stripMargin()
    }
    
    void run() {

        DataSource dataSource = Utils.createDataSource(database, database.chemcentral.username, database.chemcentral.password)
        Sql db1, db2, db3
        db1 = new Sql(dataSource.connection)
        StructureLoader loader
        
        try {
            createConcordanceTable(db1)
                  
            db2 = new Sql(dataSource.connection)
            db3 = new Sql(dataSource.connection)
            loader = new StructureLoader(db3, chemcentralStructureTable, chemcentralPropertyTable)
            db2.connection.autoCommit = false
            db2.withStatement {
                it.fetchSize = fetchSize
            }
            
            loadData(db2, db1, loader)
            
            createConcordanceIndexes(db1)
            
            int count = db1.firstRow(countConcordanceSql)[0]
            println "Number of structures is $count"
            
            int sourceId = Utils.createSourceDefinition(dataSource, database.chemcentral.schema, 1, emolecules.name, emolecules.version, emolecules.description, 'P', emolecules.owner, emolecules.maintainer, false)
            insertAliases(db1, sourceId)
            int propertyDefId = generatePropertyDefinition(db1, sourceId, count)
            generatePropertyValues(db1, [propertyDefId])
            
        } finally {
            loader?.close()
            db3?.close()
            db2?.close()
            db1?.close()
        }
    }
    
    int generatePropertyDefinition(Sql db, int sourceId, int count) {
        println "Inserting property defintion for $sourceId"
        def keys = db.executeInsert(insertPropertyDefinitionsSql, [sourceId, count])
        int id = keys[0][0]
        println "Property definition generated. ID = $id"
        return id
    }
    
    void createConcordanceTable(Sql db) {
        Utils.executeMayFail(db, "drop concordance table $concordanceTable", 'DROP TABLE ' + concordanceTable)
        Utils.execute(db, "create concordance table $concordanceTable", createConcordanceTableSql)
    }
    
    void loadData(Sql reader, Sql writer, StructureLoader loader) {
        println "Loading data from $emoleculesTable"
        
        int count = 0
        reader.eachRow(readStructuresSql) { row ->
            count++
            try {
                int cdid = loader.execute(new String(row['cd_structure']))
                //println cdid
                writer.executeInsert(insertConcordanceSql, [Math.abs(cdid), row['cd_id']])
     
            } catch (Exception ex) {
                println "WARNING: failed to process row $count"
                ex.printStackTrace()
            }
            if (count % 10000 == 0) {
                println "Handled $count rows"
            }
        }
        println "Handled $count structures"
    }
    
    void createConcordanceIndexes(Sql db) {
        println "Creating concordance indexes"
        db.execute(createStructureIdIndexSql)
        db.execute(createDBCdidIndexSql)
        println "Indexes created"
    }    
}