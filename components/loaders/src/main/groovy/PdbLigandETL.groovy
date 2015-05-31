
import groovy.sql.Sql
import java.sql.Connection
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
class PdbLigandETL extends AbstractETL {
    
    ConfigObject props
    String sourceTable, concordanceTable
    
    int limit, offset
    int fetchSize = 500
       
    static void main(String[] args) {
        println "Running with $args"
        def instance = new PdbLigandETL()
        
        instance.run()
    }
    
    PdbLigandETL() {
        props = Utils.createConfig('pdb_ligand.properties')
        
        this.sourceTable = database.vendordbs.schema + '.' + props.table
        this.concordanceTable = database.chemcentral.schema + '.' + props.table + '_concordance'
                
        this.offset = props.offset
        this.limit = props.limit
        
        generateSqls()
    }
    
    void generateSqls() {
        createConcordanceTableSql = """\
            |CREATE TABLE $concordanceTable (
            |  structure_id INTEGER NOT NULL,
            |  cd_id INTEGER NOT NULL,
            |  CONSTRAINT fk_con2stuctures FOREIGN KEY (structure_id) references $chemcentralStructureTable (cd_id) ON DELETE CASCADE,
            |  CONSTRAINT fk_con2cdid FOREIGN KEY (cd_id) references $sourceTable (cd_id) ON DELETE CASCADE
            |)""".stripMargin()
        
        readStructuresSql = "SELECT cd_id, cd_structure FROM $sourceTable".toString()
        limit && (readStructuresSql += " LIMIT $limit")
        offset && (readStructuresSql += " OFFSET $offset")
        
        insertConcordanceSql = "INSERT INTO $concordanceTable (structure_id, cd_id) VALUES (?, ?)".toString()
        
        countConcordanceSql = "SELECT count(*) FROM $concordanceTable".toString()
        
        createStructureIdIndexSql = "CREATE INDEX idx_con_pdbligand_structure_id on $concordanceTable (structure_id)"
        createDBCdidIndexSql = "CREATE INDEX idx_con_pdbligand_cd_id on $concordanceTable (cd_id)"
        
        deleteSourceSql = "DELETE FROM $chemcentralSourcesTable WHERE source_name = ?"
        
        insertPropertyDefinitionsSql = """\
                |INSERT INTO $chemcentralPropertyDefintionsTable (source_id, property_description, est_size)
                |  VALUES (?, 'PDB ligand 3D structure', ?)""".stripMargin()
        
        insertInstancesSql = """\
                |INSERT INTO $chemcentralInstancesTable
                |  (source_id, structure_id, structure_definition, description, external_id)
                |    SELECT ?, con.structure_id, db.cd_structure, 'Structure from PDB entry ' || db.pdb_code, db.full_code
                |      FROM (SELECT cd_id, cd_structure, pdb_code, full_code FROM $sourceTable) db
                |      JOIN $concordanceTable con ON con.cd_id = db.cd_id""".stripMargin()
        
        insertStructurePropsSql = """\
                |INSERT INTO $chemcentralStructurePropertiesTable
                |  (structure_id, property_def_id, instance_id, property_data)
                |  SELECT con.structure_id, ?, inst.id, row_to_json(db)::jsonb
                |    FROM (SELECT cd_id, pdb_code, full_code FROM $sourceTable) db
                |    JOIN $concordanceTable con ON con.cd_id = db.cd_id
                |    JOIN $chemcentralInstancesTable inst ON inst.external_id = db.full_code""".stripMargin()
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
            
            int sourceId = Utils.createSourceDefinition(dataSource, database.chemcentral.schema, 1, props.name, props.version, props.description, 'P', props.owner, props.maintainer, false)
            
            generateInstancesValues(db1, [sourceId])
            
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
        println "Loading data from $sourceTable"
        
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

