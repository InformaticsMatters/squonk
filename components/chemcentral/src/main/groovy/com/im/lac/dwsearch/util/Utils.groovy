package com.im.lac.dwsearch.util


import groovy.sql.Sql
import groovy.util.logging.Log
import java.sql.SQLException
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource

/**
 *
 * @author timbo
 */
@Log
class Utils {
    
    static ConfigObject createConfig(String path) {
        return createConfig(new File(path).toURL())
    }
    
    static ConfigObject createConfig(URL url) {
        return new ConfigSlurper().parse(url)
    }
    
    static void executeMayFail(Sql db, String desc, String sql) {
        println desc
        try {
            db.execute(sql)
        } catch (SQLException ex) {
            println "Execution failed: ${ex.message}"
        }
    }

    static void execute(Sql db, String desc, String sql) {
        println desc
        db.execute(sql)
    }
    
    static DataSource createDataSource(ConfigObject database, String username, String password) {
        
        PGSimpleDataSource ds = new PGSimpleDataSource()
        ds.serverName = database.server
        String port = database.port
        ds.portNumber = new Integer(port)
        ds.databaseName = database.database
        ds.user = username
        ds.password = password
        log.info("Creating datasource. server:${ds.serverName} port:${ds.portNumber} user:${ds.user}")
        return ds
    }
    
    static int createSourceDefinition(DataSource dataSource, String schema, int categoryId, String name, String desc, String type, String owner, String maintainer, boolean active) {
   
        Sql db = new Sql(dataSource)
        int id = 0 
        try {
            def rows = db.executeUpdate("""DELETE FROM ${Sql.expand(schema)}.sources WHERE
                category_id = $categoryId and source_name = $name""")
             if (rows) {
                 println "Deleted old $name"
             }
            
            def keys = db.executeInsert("""INSERT INTO ${Sql.expand(schema)}.sources
(category_id, source_name, source_description, type, owner, maintainer, active) 
values ($categoryId, $name, $desc, $type, $owner, $maintainer, $active)""")
            id = keys[0][0]
            println "Source ID is $id"
        
        } finally {
            db.close()
        }
        return id        
    }
    
     static int createPropertyDefinition(
         DataSource dataSource, 
         String schema, 
         int sourceId, 
         String desc, 
         String originalId, 
         String definition, 
         String example) {
        
        Sql db = new Sql(dataSource)
        int id = 0 
        try {
            
            def keys = db.executeInsert("""INSERT INTO ${Sql.expand(schema)}.property_definitions
(source_id, property_description, original_id, definition, example) 
values ($sourceId, $desc, $originalId, $definition, $example)""")
            id = keys[0][0]
            println "Property ID is $id"
        
        } finally {
            db.close()
        }
        return id        
    }
	
}

