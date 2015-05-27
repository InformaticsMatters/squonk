import groovy.sql.Sql
import java.sql.Connection
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
class ChemblETL extends AbstractETL {
    
    ConfigObject chembl
    String chemblIdLookupTable, chemblAssaysTable, chemblActivitiesTable, chemblCompoundStructuresTable,
    concordanceTable
    
    int limit, offset
    int fetchSize = 500
  
    private String readChemblStructuresSql, createMolregnoIndexSql
    
    static void main(String[] args) {
        println "Running with $args"
        def instance = new ChemblETL()
        instance.run()
    }
    
    ChemblETL() {
        chembl = Utils.createConfig('chembl.properties')
        
        this.concordanceTable = database.chemcentral.schema + '.chembl_' + chembl.version + '_concordance'
        this.chemblIdLookupTable = chembl.schema + '.chembl_id_lookup'
        this.chemblActivitiesTable = chembl.schema + '.activities'
        this.chemblAssaysTable = chembl.schema + '.assays'
        this.chemblCompoundStructuresTable = chembl.schema + '.compound_structures'
                
        this.offset = chembl.offset
        this.limit = chembl.limit
        
        generateSqls()
    }
    
    void generateSqls() {
        createConcordanceTableSql = """\
            |CREATE TABLE $concordanceTable (
            |  structure_id INTEGER NOT NULL,
            |  molregno INTEGER NOT NULL,
            |  CONSTRAINT fk_con2stuctures FOREIGN KEY (structure_id) references $chemcentralStructureTable (cd_id) ON DELETE CASCADE,
            |  CONSTRAINT fk_con2molregno FOREIGN KEY (molregno) references $chemblCompoundStructuresTable (molregno) ON DELETE CASCADE
            |)""".stripMargin()
        
        readChemblStructuresSql = """\
            |SELECT molregno, molfile
            |  FROM ${chembl.schema}.compound_structures""".stripMargin()
        limit && (readChemblStructuresSql += " LIMIT $limit")
        offset && (readChemblStructuresSql += " OFFSET $offset")
        
        insertConcordanceSql = "INSERT INTO $concordanceTable (structure_id, molregno) VALUES (?, ?)".toString()
        
        countConcordanceSql = "SELECT count(*) FROM $concordanceTable".toString()
        
        createStructureIdIndexSql = "CREATE INDEX idx_con_chembl_structure_id on $concordanceTable (structure_id)"
        createMolregnoIndexSql = "CREATE INDEX idx_con_chembl_molregno on $concordanceTable (molregno)"
        
        deleteAliasesSql = "DELETE FROM $chemcentralStructureAliasesTable WHERE source_id = ?"
        
        deleteSourceSql = "DELETE FROM $chemcentralSourcesTable WHERE source_name = ?"
        
        insertAliasesSql = """\
            |INSERT INTO $chemcentralStructureAliasesTable (structure_id, source_id, alias_value)
            |  SELECT con.structure_id, ?, lc.chembl_id
            |    FROM $concordanceTable con
            |    JOIN $chemblIdLookupTable lc ON lc.entity_id = con.molregno
            |      AND lc.entity_type = 'COMPOUND'""".stripMargin()
        
        insertPropertyDefinitionsSql = """\
                |INSERT INTO $chemcentralPropertyDefintionsTable (source_id, property_description, external_id, est_size)
                |  SELECT ?, COALESCE(ass.description, 'No description defined'), l.chembl_id, sub.est_size FROM
                |    (SELECT assay_id, count(*) est_size 
                |      FROM $chemblActivitiesTable act
                |      GROUP BY assay_id
                |    ) sub
                |  JOIN $chemblAssaysTable ass ON ass.assay_id = sub.assay_id
                |  JOIN $chemblIdLookupTable l ON ass.assay_id = l.entity_id
                |    AND l.entity_type = 'ASSAY'""".stripMargin()
        
        insertStructurePropsSql = """\
                |INSERT INTO $chemcentralStructurePropertiesTable
                |  (structure_id, property_def_id, property_data)
                |  SELECT con.structure_id, p.id, row_to_json(act)::jsonb
                |    FROM $chemblActivitiesTable act
                |    JOIN $chemblIdLookupTable la ON la.entity_id = act.assay_id AND la.entity_type = 'ASSAY'
                |    JOIN $chemblIdLookupTable lc ON lc.entity_id = act.molregno AND lc.entity_type = 'COMPOUND'
                |    JOIN $concordanceTable con ON con.molregno = act.molregno
                |    JOIN $chemcentralPropertyDefintionsTable p ON p.external_id = la.chembl_id""".stripMargin()
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
            
            int chemblSourceId = Utils.createSourceDefinition(dataSource, database.chemcentral.schema, 1, chembl.name, chembl.version, chembl.description, 'P', chembl.owner, chembl.maintainer, false)
            insertAliases(db1, chemblSourceId)
            generatePropertyDefinitions(db1, chemblSourceId)
            generatePropertyValues(db1, [])
            
        } finally {
            loader?.close()
            db3?.close()
            db2?.close()
            db1?.close()
        }
    }
    
    void createConcordanceTable(Sql db) {
        Utils.executeMayFail(db, 'drop concordance table', 'DROP TABLE ' + concordanceTable)
        Utils.execute(db, 'create concordance table', createConcordanceTableSql)
    }
    
    void loadData(Sql reader, Sql writer, StructureLoader loader) {
        println "Loading data from ${chembl.schema}.compound_structures"
        
        int count = 0
        reader.eachRow(readChemblStructuresSql) { row ->
            count++
            try {
                int cdid = loader.execute(row['molfile'])
                //println cdid
                writer.executeInsert(insertConcordanceSql, [Math.abs(cdid), row['molregno']])
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
        db.execute(createMolregnoIndexSql)
        println "Indexes created"
    } 
}