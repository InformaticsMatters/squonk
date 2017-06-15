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

/**
 *
 * @author timbo
 */
class AbstractETL {
    
    ConfigObject database
    
    protected String chemcentralStructureAliasesTable, chemcentralPropertyDefintionsTable,
    chemcentralStructurePropertiesTable, chemcentralStructureTable, chemcentralSourcesTable,
    chemcentralPropertyTable, chemcentralInstancesTable
    
    protected String deleteAliasesSql, insertAliasesSql, insertInstancesSql,
    createConcordanceTableSql, readStructuresSql, insertConcordanceSql, countConcordanceSql, 
    createStructureIdIndexSql, createDBCdidIndexSql,
    insertPropertyDefinitionsSql, insertStructurePropsSql, deleteSourceSql
    
    
    AbstractETL() {
        database = LoaderUtils.createConfig('database.properties')
        
        this.chemcentralInstancesTable = database.chemcentral.schema + '.instances'
        this.chemcentralStructureTable = database.chemcentral.schema + '.structures'
        this.chemcentralStructureAliasesTable = database.chemcentral.schema + '.structure_aliases'
        this.chemcentralPropertyDefintionsTable = database.chemcentral.schema + '.property_definitions'
        this.chemcentralStructurePropertiesTable = database.chemcentral.schema + '.structure_props'
        this.chemcentralSourcesTable = database.chemcentral.schema + '.sources'
        this.chemcentralPropertyTable = database.chemcentral.schema + '.jchemproperties'        
    }
    
    void insertAliases(Sql db, int sourceId) {
        println "Deleting aliases for $sourceId"
        db.execute(deleteAliasesSql, [sourceId])
        println "Inserting aliases for $sourceId"
        println "SQL: " + insertAliasesSql
        db.execute(insertAliasesSql, [sourceId])
        println "Aliases for $sourceId generated"
    }
    
    void generatePropertyDefinitions(Sql db, int sourceId) {
        println "Inserting property defintions for $sourceId"
        println "SQL: " + insertPropertyDefinitionsSql
        db.execute(insertPropertyDefinitionsSql, [sourceId])
        println "Property definitions generated"
    }
	
    void generatePropertyValues(Sql db, List params) {
        println "Inserting structure_props"
        println "SQL: " + insertStructurePropsSql
        db.execute(insertStructurePropsSql, params)
        println "structure_props values generated"
    }
    
    void generateInstancesValues(Sql db, List params) {
        println "Inserting instances"
        println "SQL: " + insertInstancesSql
        db.execute(insertInstancesSql, params)
        println "instances values generated"
    }
        
	
}

