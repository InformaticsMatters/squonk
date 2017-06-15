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

package org.squonk.camel.chemaxon.processor.enumeration

import chemaxon.formats.MolImporter
import chemaxon.struc.Molecule
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder
import org.squonk.chemaxon.molecule.MoleculeUtils
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject

/**
 *
 * @author timbo
 */
class ReactorProcessorSpec extends CamelSpecificationBase {


    void "react simple"() {
        setup:
        List mols = Molecules.nci100Molecules()
        mols.eachWithIndex { mo, c ->
            mo.putValue("R1_REACTANT", "R1_" + (c+1))
            mo.putValue("R2_REACTANT", "R2_" + (c+1))
        }
        Dataset dataset = new Dataset(MoleculeObject.class, mols)

        println "Read: ${mols.size()} reactants"
        def headers = [(ReactorProcessor.OPTION_REACTOR_REACTION):"Acylation_of_amines"]

        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        long t0 = System.currentTimeMillis()
        template.sendBodyAndHeaders('direct:start', dataset, headers)
        
        then:
        resultEndpoint.assertIsSatisfied()
        def stream = resultEndpoint.receivedExchanges.in.body[0]
        println "Stream: " + stream
        def results = stream.collect()
        long t1 = System.currentTimeMillis()
        println "Number of products: ${results.size()} generated in ${t1-t0}ms"
        results.size() > 0
    }
    
    
    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                .process(new ReactorProcessor("../../docker/deploy/images/chemservices/chemaxon_reaction_library.zip", null))
                .to('mock:result')
            }
        }
    }
}

