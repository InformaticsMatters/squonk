import chemaxon.struc.Molecule
import groovy.sql.Sql
import java.sql.SQLException
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource

/**
 *
 * @author timbo
 */
class LoaderUtils {
    
    static ConfigObject createConfig(String path) {
        return createConfig(new File(path).toURI().toURL())
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
    
    static DataSource createDataSource(ConfigObject database, String user, String password) {
        println """Creating datasource:
  server: ${database.server}
  port: ${database.port}
  database: ${database.database}
  user: $user
  password: $password
"""
        PGSimpleDataSource ds = new PGSimpleDataSource()
        ds.serverName = database.server
        ds.portNumber = new Integer(database.port)
        ds.databaseName = database.database
        ds.user = user
        ds.password = password
        
        return ds
    }
    
    static int createSourceDefinition(DataSource dataSource, String schema, int categoryId, String name, String version, String desc, String type, String owner, String maintainer, boolean active) {
   
        Sql db = new Sql(dataSource)
        int id = 0 
        try {
            def rows = db.executeUpdate("""DELETE FROM ${Sql.expand(schema)}.sources WHERE
                category_id = $categoryId AND source_name = $name AND source_version = $version""")
             if (rows) {
                 println "Deleted old $name"
             }
            
            def keys = db.executeInsert("""INSERT INTO ${Sql.expand(schema)}.sources
(category_id, source_name, source_version, source_description, type, owner, maintainer, active) 
values ($categoryId, $name, $version, $desc, $type, $owner, $maintainer, $active)""")
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
    
    /** Finds the parent structure. If there is only one fragement it returns the 
     * input molecule (same instance). If there are multipel fragements if returns 
     * the biggest by atom count. If multiple fragements have the same mumber of 
     * atoms then the one with the biggest mass is returned. If multiple ones have 
     * the same atom count and mass it is assumed they are the same (which is not
     * necessarily the case) and the first is returned.
     * 
     * @return The parent fragment, or null if none can be found
    */
    static Molecule findParentStructure(Molecule mol) {
        Molecule[] frags = mol.convertToFrags()
        if (frags.length == 1) {
            return mol
        } else {
            int maxAtoms = 0
            def biggestByAtomCount = []
            frags.each { f ->
                int ac = f.atomCount
                if (ac > maxAtoms) {
                    biggestByAtomCount.clear()
                    biggestByAtomCount << f
                    maxAtoms = ac
                } else if(f.atomCount == maxAtoms) {
                    biggestByAtomCount << f
                } 
            }
            if (biggestByAtomCount.size() == 1) {
                return biggestByAtomCount[0]
            } else if (biggestByAtomCount.size() > 1) {
                def biggestByMass = []
                double maxMass = 0
                biggestByAtomCount.each { f ->
                    double mass = f.mass
                    if (mass > maxMass) {
                        biggestByMass.clear()
                        biggestByMass << f
                        maxMass = mass
                    } else if(f.mass == maxMass) {
                        biggestByMass << f
                    }
                }
                if (biggestByMass.size() > 0) {
                    return biggestByMass[0]
                } else { // strange?
                    return null
                }
            } else { // strange?
                return null
            }
        }
    }
	
}

