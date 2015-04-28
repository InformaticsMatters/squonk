
import groovy.sql.Sql
import java.sql.Connection
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
    
    static final String DATASET_NAME = 'DrugBank'
    
//    protected String createConcordanceTableSql, readStructuresSql, insertConcordanceSql, 
//    createStructureIdIndexSql, createDBCdidIndexSql,
//    insertPropertyDefinitionsSql, insertStructurePropsSql, deleteSourceSql
//    
    static void main(String[] args) {
        println "Running with $args"
        def instance = new DrugBankETL()
        
        instance.run()
    }
    
    DrugBankETL() {
        drugbank = Utils.createConfig('drugbank.properties')
        
        this.drugBankTable = database.vendordbs.schema + '.' + drugbank.table
        this.concordanceTable = this.drugBankTable + '_concordance'
                
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
        
        createStructureIdIndexSql = "CREATE INDEX idx_con_drugbank_structure_id on $concordanceTable (structure_id)"
        createDBCdidIndexSql = "CREATE INDEX idx_con_drugbank_cd_id on $concordanceTable (cd_id)"
        
        deleteAliasesSql = "DELETE FROM $chemcentralStructureAliasesTable WHERE alias_type = ?"
        
        deleteSourceSql = "DELETE FROM $chemcentralSourcesTable WHERE source_name = ?"
        
        insertAliasesSql = """\
            |INSERT INTO $chemcentralStructureAliasesTable (structure_id, alias_type, alias_value)
            |  SELECT con.structure_id, '$DATASET_NAME', db.drugbank_id
            |    FROM $concordanceTable con
            |    JOIN $drugBankTable db ON db.cd_id = con.cd_id""".stripMargin()
        
        insertPropertyDefinitionsSql = """\
                |INSERT INTO $chemcentralPropertyDefintionsTable (source_id, property_description, original_id, est_size)
                |  VALUES (?, 'DrugBank record', null, ${drugbank.estSize})""".stripMargin()
        
        insertStructurePropsSql = """\
                |INSERT INTO $chemcentralStructurePropertiesTable
                |  (structure_id, source_id, batch_id, property_id, property_data)
                |  SELECT con.structure_id, ?, db.drugbank_id, ?, row_to_json(db)::jsonb
                |    FROM (SELECT cd_id, drugbank_id, drug_groups, generic_name, brands FROM $drugBankTable) db
                |    JOIN $concordanceTable con ON con.cd_id = db.cd_id""".stripMargin()

    }
    
    void run() {

        DataSource dataSource = Utils.createDataSource(database, chemcentral.username, chemcentral.password)
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
            
            deleteSource(db1, DATASET_NAME)
            int sourceId = Utils.createSourceDefinition(dataSource, chemcentral.schema, 1, drugbank.name, drugbank.description, 'P', drugbank.owner, drugbank.maintainer, true)
            insertAliases(db1, DATASET_NAME)
            int propertyId = generatePropertyDefinition(db1, sourceId)
            generatePropertyValues(db1, [sourceId, propertyId])
            
        } finally {
            loader?.close()
            db3?.close()
            db2?.close()
            db1?.close()
        }
    }
    
    int generatePropertyDefinition(Sql db, int sourceId) {
        println "Inserting property defintion for $sourceId"
        def keys = db.executeInsert(insertPropertyDefinitionsSql, [sourceId])
        int id = keys[0][0]
        println "Property definition generated. ID = $id"
        return id
    }
    
    void createConcordanceTable(Sql db) {
        Utils.executeMayFail(db, 'drop concordance table $concordanceTable', 'DROP TABLE ' + concordanceTable)
        Utils.execute(db, 'create concordance table $concordanceTable', createConcordanceTableSql)
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

