package rdkit

import groovy.sql.Sql
import org.postgresql.ds.PGSimpleDataSource

import javax.sql.DataSource
import java.sql.SQLException

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

}

