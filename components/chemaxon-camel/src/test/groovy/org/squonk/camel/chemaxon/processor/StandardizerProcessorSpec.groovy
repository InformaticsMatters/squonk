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

package org.squonk.camel.chemaxon.processor

import chemaxon.struc.Molecule
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.types.MoleculeObject
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class StandardizerProcessorSpec extends CamelSpecificationBase {


    def resultEndpoint

    def 'standardizer for list'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mols = []
        mols << new MoleculeObject('c1ccccc1')
        template.sendBody('direct:start', mols)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0].getStream().collect()
        result.size() == 1
        result[0].getRepresentation(Molecule.class.getName()) != null
        result[0].getRepresentation(Molecule.class.getName()).atomCount == 12
        
    }
    
    def 'standardizer for single'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mol = new MoleculeObject('c1ccccc1')
        template.sendBody('direct:start', mol)

        then:
        resultEndpoint.assertIsSatisfied()
        MoleculeObject result = resultEndpoint.receivedExchanges.in.body[0]
        result.getRepresentation(Molecule.class.getName()) != null
        result.getRepresentation(Molecule.class.getName()).atomCount == 12
        
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                .process(new StandardizerProcessor('addexplicith'))
                .to('mock:result')
            }
        }
    }
}
