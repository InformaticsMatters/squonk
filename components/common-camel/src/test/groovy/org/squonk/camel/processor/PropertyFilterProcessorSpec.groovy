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
import static org.squonk.util.CommonConstants.*
import spock.lang.Specification

/**
 * Created by timbo on 29/05/16.
 */
class PropertyFilterProcessorSpec extends Specification {

    static List mols = [
            new MoleculeObject("mol1", "smiles", [a:1, b:1.1]),
            new MoleculeObject("mol2", "smiles", [a:2, b:2.2]),
            new MoleculeObject("mol3", "smiles", [a:3, b:3.3]),
            new MoleculeObject("mol4", "smiles", [a:4, b:4.4]),
            new MoleculeObject("mol5", "smiles", [a:5, b:5.5]),
            new MoleculeObject("mol5", "smiles", [:])
    ]

    static CamelContext context

    void setupSpec() {

        context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {

            @Override
            void configure() throws Exception {

                from("direct:preset_nulls_fail")
                        .process(new PropertyFilterProcessor("fieldName")
                .filterInteger("a", false, 2, 4)
                .filterDouble("b", false, 3.0d, 10.0d))

                from("direct:preset_nulls_pass")
                        .process(new PropertyFilterProcessor("fieldName")
                        .filterInteger("a", true, 2, 4)
                        .filterDouble("b", true, 3.0d, 10.0d))

                from("direct:dynamic")
                        .process(new PropertyFilterProcessor("fieldName")
                        .filterInteger("a")
                        .filterDouble("b"))
            }
        })

        context.start()
    }

    void cleanupSpec() {
        context.stop()
    }


    void "preset nulls fail check"() {

        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        pt.requestBodyAndHeaders("direct:preset_nulls_fail", new Dataset(MoleculeObject.class, mols), [(OPTION_FILTER_MODE): mode]).getItems().size() == count

        where:
        count | mode
        2     | VALUE_INCLUDE_PASS
        4     | VALUE_INCLUDE_FAIL
        6     | VALUE_INCLUDE_ALL
    }

    void "preset nulls pass check"() {

        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        pt.requestBodyAndHeaders("direct:preset_nulls_pass", new Dataset(MoleculeObject.class, mols), [(OPTION_FILTER_MODE): mode]).getItems().size() == count

        where:
        count | mode
        3     | VALUE_INCLUDE_PASS
        3     | VALUE_INCLUDE_FAIL
        6     | VALUE_INCLUDE_ALL
    }

    void "dynamic check"() {

        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        pt.requestBodyAndHeaders("direct:dynamic", new Dataset(MoleculeObject.class, mols), headers).getItems().size() == count

        where:
        count | headers
        2     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_PASS, "a": "2|4", "b": "3.0|10.0"]
        4     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_FAIL, "a": "2|4", "b": "3.0|10.0"]
        6     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_ALL, "a": "2|4", "b": "3.0|10.0"]
        4     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_PASS, "a": "|4", "b": "|10.0"]
        2     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_PASS, "a": "2|3"]

    }


    void "return MoleculeObjectDataset"() {

        ProducerTemplate pt = context.createProducerTemplate()

        when:
        def result = pt.requestBodyAndHeaders("direct:preset_nulls_pass", new Dataset(MoleculeObject.class, mols), [(OPTION_FILTER_MODE): VALUE_INCLUDE_PASS])

        then:
        result instanceof MoleculeObjectDataset

    }

}
