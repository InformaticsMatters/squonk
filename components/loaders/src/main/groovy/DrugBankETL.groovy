
import groovy.sql.Sql

import javax.sql.DataSource

/**
 *
 * @author timbo
 */
class DrugBankETL extends AbstractETL {
    
    ConfigObject drugbank
    String drugBankTable, concordanceTable
    
    int limit, offset
    int fetchSize = 500
    
    static void main(String[] args) {
        println "Running with $args"
        def instance = new DrugBankETL()
        
        instance.run()
    }
    
    DrugBankETL() {
        drugbank = LoaderUtils.createConfig('drugbank.properties')
        
        this.drugBankTable = database.vendordbs.schema + '.' + drugbank.table
        this.concordanceTable = database.chemcentral.schema + '.concordance_' + drugbank.table
                
        this.offset = drugbank.offset
        this.limit = drugbank.limit
        
        generateSqls()
    }
    
    void generateSqls() {
        createConcordanceTableSql = """\
            |CREATE TABLE $concordanceTable (
            |  structure_id INTEGER NOT NULL,
            |  cd_id INTEGER NOT NULL,
            |  CONSTRAINT fk_con2stuctures FOREIGN KEY (structure_id) references $chemcentralStructureTable (cd_id) ON DELETE CASCADE,
            |  CONSTRAINT fk_con2cdid FOREIGN KEY (cd_id) references $drugBankTable (cd_id) ON DELETE CASCADE
            |)""".stripMargin()
        
        readStructuresSql = "SELECT cd_id, cd_structure FROM $drugBankTable".toString()
        limit && (readStructuresSql += " LIMIT $limit")
        offset && (readStructuresSql += " OFFSET $offset")
        
        insertConcordanceSql = "INSERT INTO $concordanceTable (structure_id, cd_id) VALUES (?, ?)".toString()
        
        countConcordanceSql = "SELECT count(*) FROM $concordanceTable".toString()
        
        createStructureIdIndexSql = "CREATE INDEX idx_con_drugbank_structure_id on $concordanceTable (structure_id)"
        createDBCdidIndexSql = "CREATE INDEX idx_con_drugbank_cd_id on $concordanceTable (cd_id)"
        
        deleteAliasesSql = "DELETE FROM $chemcentralStructureAliasesTable WHERE source_id = ?"
        
        deleteSourceSql = "DELETE FROM $chemcentralSourcesTable WHERE source_name = ?"
        
        insertAliasesSql = """\
            |INSERT INTO $chemcentralStructureAliasesTable (structure_id, source_id, alias_value)
            |  SELECT con.structure_id, ?, db.drugbank_id
            |    FROM $concordanceTable con
            |    JOIN $drugBankTable db ON db.cd_id = con.cd_id""".stripMargin()
        
        insertPropertyDefinitionsSql = """\
                |INSERT INTO $chemcentralPropertyDefintionsTable (source_id, property_description, est_size)
                |  VALUES (?, 'DrugBank record', ?)""".stripMargin()
        
        insertStructurePropsSql = """\
                |INSERT INTO $chemcentralStructurePropertiesTable
                |  (structure_id, property_def_id, property_data)
                |  SELECT con.structure_id, ?, row_to_json(db)::jsonb
                |    FROM (SELECT cd_id, drugbank_id, drug_groups, generic_name, brands FROM $drugBankTable) db
                |    JOIN $concordanceTable con ON con.cd_id = db.cd_id""".stripMargin()
    }
    
    void run() {

        DataSource dataSource = LoaderUtils.createDataSource(database, database.chemcentral.username, database.chemcentral.password)
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
            
            int sourceId = LoaderUtils.createSourceDefinition(dataSource, database.chemcentral.schema, 1, drugbank.name, drugbank.version, drugbank.description, 'P', drugbank.owner, drugbank.maintainer, false)
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
        LoaderUtils.executeMayFail(db, "drop concordance table $concordanceTable", 'DROP TABLE ' + concordanceTable)
        LoaderUtils.execute(db, "create concordance table $concordanceTable", createConcordanceTableSql)
    }
    
    void loadData(Sql reader, Sql writer, StructureLoader loader) {
        println "Loading data from $drugBankTable"
        
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

            if (count % 1000 == 0) {
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

