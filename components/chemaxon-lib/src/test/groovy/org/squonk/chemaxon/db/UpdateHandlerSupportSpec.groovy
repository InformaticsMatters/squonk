package org.squonk.chemaxon.db


import chemaxon.jchem.db.TableTypeConstants
import chemaxon.jchem.db.UpdateHandler

import groovy.sql.Sql
import java.sql.*
import spock.lang.Shared
import spock.lang.Specification


/**
 *
 * @author timbo
 */
class UpdateHandlerSupportSpec extends Specification {
    
    @Shared Connection con
    @Shared UpdateHandlerSupport uhs
    @Shared Sql db
    
    def setupSpec() {
        con = DriverManager.getConnection("jdbc:derby:memory:UpdateHandlerSupportSpec;create=true")
        db = new Sql(con)
        uhs = new UpdateHandlerSupport()
        uhs.connection = con
        uhs.start()
        uhs.createPropertyTable(true)
    } 
    def cleanupSpec() {
        uhs.stop()
        try {
            DriverManager.getConnection("jdbc:derby:memory:UpdateHandlerSupportSpec;drop=true")
        } catch (SQLException ex) {} // expected
    }
    
    def 'create structure table'() {
        
        when:
        uhs.createStructureTable("testcreate", TableTypeConstants.TABLE_TYPE_MOLECULES, null, null)

        then:
        db.firstRow("select count(*) from testcreate")[0] == 0
    }
    
    
    def 'drop structure table'() {

        given:
        uhs.dropStructureTable("testcreate", true)
        
        when:
        db.firstRow("select count(*) from testcreate")

        then:
        thrown(SQLException)
    }
    
    def 'test insert'() {

        setup:
        uhs.createStructureTable("testinsert", TableTypeConstants.TABLE_TYPE_MOLECULES, null, 
        'col1 VARCHAR(100), col2 INTEGER, col3 DOUBLE')
        def worker = uhs.createWorker(UpdateHandler.INSERT, "testinsert", 'col1, col2, col3')
        
        when:
        worker.execute('C', 'mercury', 1, 1.11d)
        worker.execute('CC', 'venus', 2, 2.22d)
        worker.execute('CCC', 'earth', 3, 3.33d)
        worker.close()
        

        then:
        db.firstRow("select count(*) from testinsert")[0] == 3
        
        cleanup:
        worker?.close()
        
    }
	
}

