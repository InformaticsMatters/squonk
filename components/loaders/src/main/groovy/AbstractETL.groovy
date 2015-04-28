import groovy.sql.Sql

/**
 *
 * @author timbo
 */
class AbstractETL {
    
    ConfigObject chemcentral, database
    
    protected String chemcentralStructureAliasesTable, chemcentralPropertyDefintionsTable,
    chemcentralStructurePropertiesTable, chemcentralStructureTable, chemcentralSourcesTable,
    chemcentralPropertyTable
    
    protected String deleteAliasesSql, insertAliasesSql 
    protected String createConcordanceTableSql, readStructuresSql, insertConcordanceSql, 
    createStructureIdIndexSql, createDBCdidIndexSql,
    insertPropertyDefinitionsSql, insertStructurePropsSql, deleteSourceSql
    
    
    AbstractETL() {
        chemcentral = Utils.createConfig('chemcentral.properties')
        database = Utils.createConfig('database.properties')
        
        this.chemcentralStructureTable = chemcentral.schema + '.structures'
        this.chemcentralStructureAliasesTable = chemcentral.schema + '.structure_aliases'
        this.chemcentralPropertyDefintionsTable = chemcentral.schema + '.property_definitions'
        this.chemcentralStructurePropertiesTable = chemcentral.schema + '.structure_props'
        this.chemcentralSourcesTable = chemcentral.schema + '.sources'
        this.chemcentralPropertyTable = chemcentral.schema + '.jchemproperties'        
    }
    
    void insertAliases(Sql db, String sourceName) {
        println "Deleting alises for $sourceName"
        db.execute(deleteAliasesSql, [sourceName])
        println "Inserting alises for $sourceName"
        db.execute(insertAliasesSql)
        println "Alises for $sourceName generated"
    }
    
    void deleteSource(Sql db, String sourceName) {
        println "Deleting source $sourceName"
        db.execute(deleteSourceSql, [sourceName])
        println "$sourceName deleted"
    }
    
    void generatePropertyDefinitions(Sql db, int sourceId) {
        println "Inserting property defintions for $sourceId"
        db.execute(insertPropertyDefinitionsSql, [sourceId])
        println "Property definitions generated"
    }
	
    void generatePropertyValues(Sql db, List params) {
        println "Inserting structure_props"
        db.execute(insertStructurePropsSql, params)
        println "structure_props values generated"
    }
	
}

