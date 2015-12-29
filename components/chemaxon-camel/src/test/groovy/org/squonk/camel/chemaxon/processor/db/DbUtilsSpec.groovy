package org.squonk.camel.chemaxon.processor.db

import groovy.sql.Sql
import java.sql.*
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DBUtilsSpec extends Specification {
    
    @Shared Connection con
    @Shared Sql db
    
    def setupSpec() {
        con = DriverManager.getConnection("jdbc:derby:memory:JChemDBSearcherSpec;create=true")
        db = new Sql(con)
        
    } 
    def cleanupSpec() {
        try {
            DriverManager.getConnection("jdbc:derby:memory:JChemDBSearcherSpec;drop=true")
        } catch (SQLException ex) {} // expected
    }
    
//    def 'create nci1000 structure table'() {
//        when:
//        long t0 = System.currentTimeMillis()
//        DBUtils.createNci1000StructureTable(con, 'nci1000')
//        long t1 = System.currentTimeMillis()
//        println "creating nci1000 took ${t1-t0}ms"
//        
//        then:
//        db.firstRow("select count(*) from nci1000")[0] == 1000
//    }
//    
//    def 'create dhfr structure table'() {
//        when:
//        long t0 = System.currentTimeMillis()
//        DBUtils.createDHFRStructureTable(con, 'dhfr')
//        long t1 = System.currentTimeMillis()
//        println "creating DHFR took ${t1-t0}ms"
//        
//        then:
//        db.firstRow("select count(*) from dhfr")[0] == 756
//    }
	
}

