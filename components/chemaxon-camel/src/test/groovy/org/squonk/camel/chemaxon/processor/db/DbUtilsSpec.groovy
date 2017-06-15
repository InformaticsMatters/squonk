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

