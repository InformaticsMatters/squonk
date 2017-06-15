/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import groovy.sql.Sql

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
        chembl = LoaderUtils.createConfig('chembl.properties')
        
        this.concordanceTable = database.chemcentral.schema + '.concordance_chembl_' + chembl.version
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
        
        //        insertStructurePropsSql = """\
        //                |INSERT INTO $chemcentralStructurePropertiesTable
        //                |  (structure_id, property_def_id, property_data)
        //                |  SELECT con.structure_id, p.id, ('{' ||
        //                |    '"activity_id":' || activity_id ||
        //                |    ',"assay_id":' || assay_id ||
        //                |    ',"doc_id":' || doc_id ||
        //                |    ',"record_id":' || record_id ||
        //                |    ',"molregno":' || act.molregno ||
        //                |    CASE WHEN act.standard_relation IS NULL THEN '' ELSE ',"standard_relation":"' || act.standard_relation || '"' END ||
        //                |    CASE WHEN act.standard_value IS NULL THEN '' ELSE ',"standard_value":' || act.standard_value END ||
        //                |    CASE WHEN act.standard_units IS NULL THEN '' ELSE ',"standard_units":"' || act.standard_units || '"' END ||
        //                |    CASE WHEN act.standard_flag IS NULL THEN '' ELSE ',"standard_flag":' || act.standard_flag END ||
        //                |    CASE WHEN act.standard_type IS NULL THEN '' ELSE ',"standard_type":"' || act.standard_type || '"' END ||
        //                |    CASE WHEN act.data_validity_comment IS NULL THEN '' ELSE ',"data_validity_comment":"' || act.data_validity_comment || '"' END ||
        //                |    CASE WHEN act.potential_duplicate IS NULL THEN '' ELSE ',"potential_duplicate":"' || act.potential_duplicate || '"' END ||
        //                |    CASE WHEN act.pchembl_value IS NULL THEN '' ELSE ',"pchembl_value":' || act.pchembl_value END ||
        //                |    CASE WHEN act.activity_comment IS NULL THEN '' ELSE ',"activity_comment":' || to_json(act.activity_comment) END ||
        //                |    '}')::jsonb
        //                |    FROM $chemblActivitiesTable act
        //                |    JOIN $chemblIdLookupTable la ON la.entity_id = act.assay_id AND la.entity_type = 'ASSAY'
        //                |    JOIN $chemblIdLookupTable lc ON lc.entity_id = act.molregno AND lc.entity_type = 'COMPOUND'
        //                |    JOIN $concordanceTable con ON con.molregno = act.molregno
        //                |    JOIN $chemcentralPropertyDefintionsTable p ON p.external_id = la.chembl_id""".stripMargin()
    
        insertStructurePropsSql = """\
        |INSERT INTO $chemcentralStructurePropertiesTable
        |  (structure_id, property_def_id, property_data)
        |  SELECT con.structure_id, p.id, data.json FROM (
        |    SELECT act.molregno, act.assay_id, act.doc_id, ('[' || (string_agg('{' ||
        |      '"activity_id":' || activity_id ||
        |      ',"assay_id":'   || assay_id ||
        |      ',"doc_id":'     || doc_id ||
        |      ',"record_id":'  || record_id ||
        |      ',"molregno":'   || act.molregno ||
        |      CASE WHEN act.standard_relation     IS NULL THEN '' ELSE ',"standard_relation":"'     || act.standard_relation || '"' END ||
        |      CASE WHEN act.standard_value        IS NULL THEN '' ELSE ',"standard_value":'         || act.standard_value END ||
        |      CASE WHEN act.standard_units        IS NULL THEN '' ELSE ',"standard_units":"'        || act.standard_units || '"' END ||
        |      CASE WHEN act.standard_flag         IS NULL THEN '' ELSE ',"standard_flag":'          || act.standard_flag END ||
        |      CASE WHEN act.standard_type         IS NULL THEN '' ELSE ',"standard_type":"'         || act.standard_type || '"' END ||
        |      CASE WHEN act.data_validity_comment IS NULL THEN '' ELSE ',"data_validity_comment":'  || to_json(act.data_validity_comment) END ||
        |      CASE WHEN act.potential_duplicate   IS NULL THEN '' ELSE ',"potential_duplicate":"'   || act.potential_duplicate || '"' END ||
        |      CASE WHEN act.pchembl_value         IS NULL THEN '' ELSE ',"pchembl_value":'          || act.pchembl_value END ||
        |      CASE WHEN act.activity_comment      IS NULL THEN '' ELSE ',"activity_comment":'       || to_json(act.activity_comment) END ||
        |      '}',',')) || ']')::jsonb AS json
        |      FROM $chemblActivitiesTable act
        |      GROUP BY act.molregno, act.assay_id, act.doc_id
        |    ) data
        |    JOIN $chemblIdLookupTable la ON la.entity_id = data.assay_id AND la.entity_type = 'ASSAY'
        |    JOIN $chemblIdLookupTable lc ON lc.entity_id = data.molregno AND lc.entity_type = 'COMPOUND'
        |    JOIN $concordanceTable con ON con.molregno = data.molregno
        |    JOIN $chemcentralPropertyDefintionsTable p ON p.external_id = la.chembl_id""".stripMargin()
    
        //                |    CASE WHEN act.published_relation IS NULL THEN '' ELSE ',"published_relation":"' || act.published_relation || '"' END ||
        //                |    CASE WHEN act.published_value IS NULL THEN '' ELSE ',"published_value":' || act.published_value END ||
        //                |    CASE WHEN act.published_units IS NULL THEN '' ELSE ',"published_units":"' || act.published_units || '"' END ||
        //                |    CASE WHEN act.published_type IS NULL THEN '' ELSE ',"published_type":"' || act.published_type || '"' END ||
        //                |    CASE WHEN act.bao_endpoint IS NULL THEN '' ELSE ',"bao_endpoint":"' || act.bao_endpoint || '"' END ||
        //                |    CASE WHEN act.uo_units IS NULL THEN '' ELSE ',"uo_units":"' || act.uo_units || '"' END ||
        //                |    CASE WHEN act.qudt_units IS NULL THEN '' ELSE ',"qudt_units":"' || act.qudt_units || '"' END ||
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
            
            int chemblSourceId = LoaderUtils.createSourceDefinition(dataSource, database.chemcentral.schema, 1, chembl.name, chembl.version, chembl.description, 'P', chembl.owner, chembl.maintainer, false)
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
        LoaderUtils.executeMayFail(db, 'drop concordance table', 'DROP TABLE ' + concordanceTable)
        LoaderUtils.execute(db, 'create concordance table', createConcordanceTableSql)
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