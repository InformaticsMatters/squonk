/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

import chemaxon.jchem.db.DatabaseProperties
import chemaxon.jchem.db.StructureTableOptions
import chemaxon.jchem.db.UpdateHandler
import chemaxon.util.ConnectionHandler
import org.apache.camel.CamelContext
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.types.MoleculeObject
import org.apache.camel.builder.RouteBuilder
import spock.lang.Shared

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Created by timbo on 14/04/2014.
 */
class DefaultJChemInserterSpecification extends CamelSpecificationBase {


    def resultEndpoint
    def updateHandlerProcessor
    static def db = 'jdbc:derby:memory:AbstractUpdateHandlerProcessorSpecification'

    @Shared
    Connection con


    def setupSpec() { // run before the first feature method
        println "creating db and connection"
        con = DriverManager.getConnection(db + ';create=true')

        println "creating connection handler"
        ConnectionHandler conh = new ConnectionHandler()
        conh.connection = con

        DatabaseProperties.createPropertyTable(conh)
        UpdateHandler.createStructureTable(conh, new StructureTableOptions('TEST'))
    }

    def cleanupSpec() { // run after the last feature method
        println "shutting down db"
        try {
            DriverManager.getConnection(db + ';shutdown=true')
        } catch (SQLException e) {
            //println "shutdown successful"
        }
    }

    def 'simple structure as text insert'() {
        
        println "simple structure as text insert()"

        setup:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(2)

        when:
        template.sendBody('direct:start', 'c1ccccc1')
        template.sendBody('direct:start', 'c1ccncc1')

        then:
        resultEndpoint.assertIsSatisfied()
        updateHandlerProcessor.executionCount == 2
        updateHandlerProcessor.errorCount == 0
    }
    
    def 'simple structure as MoleculeObject insert'() {
        
        println 'simple structure as MoleculeObject insert()'

        setup:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(2)

        when:
        template.sendBody('direct:start', new MoleculeObject('c1ccccc1'))
        template.sendBody('direct:start', new MoleculeObject('c1ccncc1'))

        then:
        resultEndpoint.assertIsSatisfied()
        updateHandlerProcessor.executionCount == 2
        updateHandlerProcessor.errorCount == 0
    }

    @Override
    void addRoutes(CamelContext context) {

        println "creating route builder"
        ConnectionHandler conh = new ConnectionHandler()
        conh.connection = con

        updateHandlerProcessor = new DefaultJChemInserter('TEST', null, [:])
        updateHandlerProcessor.connectionHandler = conh

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from('direct:start')
                .log('Processing data ${body}')
                .process(updateHandlerProcessor)
                .to('mock:result')
            }
        })
    }
}
