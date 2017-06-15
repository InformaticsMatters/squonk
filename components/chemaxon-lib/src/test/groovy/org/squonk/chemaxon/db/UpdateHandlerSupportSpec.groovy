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

