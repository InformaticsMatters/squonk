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

package org.squonk.camel.processor

import org.squonk.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.util.CommonConstants
import spock.lang.Specification

/**
 * Created by timbo on 29/05/16.
 */
class VerifyStructureProcessorSpec extends Specification {

    static List mols = [
            new MoleculeObject("a", "smiles"),
            new MoleculeObject("a", "smiles"),
            new MoleculeObject("b", "smiles"),
            new MoleculeObject("a", "smiles")
    ]

    static CamelContext context

    void setupSpec() {

        context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {

            @Override
            void configure() throws Exception {
                from("direct:myroute")
                        .process(new VerifyStructureProcessor("fieldName") {
                        protected boolean validateMolecule(MoleculeObject mo) {
                            mo.getSource() == "a"
                    }
                })
            }
        })

        context.start()
    }

    void cleanupSpec() {
        context.stop()
    }


    void "filter pass"() {

        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        pt.requestBodyAndHeaders("direct:myroute", new Dataset(MoleculeObject.class, mols), [(CommonConstants.OPTION_FILTER_MODE): mode]).getItems().size() == count

        where:
        count | mode
        3     | CommonConstants.VALUE_INCLUDE_PASS
        1     | CommonConstants.VALUE_INCLUDE_FAIL
        4     | CommonConstants.VALUE_INCLUDE_ALL

    }


    void "return MoleculeObjectDataset"() {

        ProducerTemplate pt = context.createProducerTemplate()

        when:
        def result = pt.requestBodyAndHeaders("direct:myroute", new Dataset(MoleculeObject.class, mols), [(CommonConstants.OPTION_FILTER_MODE): CommonConstants.VALUE_INCLUDE_PASS])

        then:
        result instanceof MoleculeObjectDataset

    }

}
