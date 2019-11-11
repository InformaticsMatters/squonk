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

package org.squonk.execution.steps.impl

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetEnricherStepSpec extends Specification {

    def uuid1 = UUID.randomUUID()
    def uuid2 = UUID.randomUUID()
    def uuid3 = UUID.randomUUID()

    def objs1() {
        [
                new BasicObject(uuid1, [id: 1, a: "1", hello: 'world']),
                new BasicObject(uuid2, [id: 2, a: "99", hello: 'mars']),
                new BasicObject(uuid3, [id: 3, a: "100", hello: 'mum'])
        ]
    }

    def objs2() {
        [
                new BasicObject(uuid1, [id: 1, b: "1", hello: 'jupiter']),
                new BasicObject(uuid2, [id: 2, b: "99", hello: 'saturn', foo: 'baz']),
        ]
    }


    def mols1() {
        [
                new MoleculeObject(uuid1, "C", "smiles", [id: 1, c: "11"]),
                new MoleculeObject(uuid2, "CC", "smiles", [id: 2, c: "999"]),
                new MoleculeObject(uuid3, "CCC", "smiles", [id: 4, c: "100"])
        ]
    }

    def mols2() {
        [
                new MoleculeObject(uuid1, "NNC", "mol", [id: 1, c: "1000"]),
                new MoleculeObject(uuid2, "NNCC", "smiles", [id: 2, c: "1000"]),
                new MoleculeObject(uuid3, "NNCCC", "smiles", [id: 4, c: "1000"])
        ]
    }

    void "merge bo datasets using field"() {

        DefaultCamelContext context = new DefaultCamelContext()

        Dataset ds1 = new Dataset(BasicObject.class, objs1())
        Dataset ds2 = new Dataset(BasicObject.class, objs2())
        def inputs = [(DatasetEnricherStep.VAR_INPUT): ds1, (DatasetEnricherStep.VAR_NEW_DATA): ds2]

        DatasetEnricherStep step = new DatasetEnricherStep()
        step.configure(
                "merge bo datasets using field",
                [(DatasetEnricherStep.OPT_MAIN_FIELD): 'id', (DatasetEnricherStep.OPT_EXTRA_FIELD): 'id'],
                DatasetEnricherStep.SERVICE_DESCRIPTOR,
                context, null)

        when:
        def resultsMap = step.doExecute(inputs)
        def result = resultsMap["output"]

        then:
        result != null
        def items = result.items

        testBOValues(items)

    }

    void "merge bo datasets using uuid"() {

        DefaultCamelContext context = new DefaultCamelContext()

        Dataset ds1 = new Dataset(BasicObject.class, objs1())
        Dataset ds2 = new Dataset(BasicObject.class, objs2())
        def inputs = [(DatasetEnricherStep.VAR_INPUT): ds1, (DatasetEnricherStep.VAR_NEW_DATA): ds2]

        DatasetEnricherStep step = new DatasetEnricherStep()
        step.configure("merge bo datasets using uuid", null,
                DatasetEnricherStep.SERVICE_DESCRIPTOR,
                context, null)

        when:
        def resultsMap = step.doExecute(inputs)
        def result = resultsMap["output"]

        then:
        result != null
        def items = result.items

        testBOValues(items)

    }

    void "merge mo datasets using field"() {

        DefaultCamelContext context = new DefaultCamelContext()

        Dataset ds1 = new Dataset(MoleculeObject.class, mols1())
        Dataset ds2 = new Dataset(MoleculeObject.class, mols2())
        def inputs = [(DatasetEnricherStep.VAR_INPUT): ds1, (DatasetEnricherStep.VAR_NEW_DATA): ds2]

        DatasetEnricherStep step = new DatasetEnricherStep()
        step.configure("merge mo datasets using field",
                [(DatasetEnricherStep.OPT_MAIN_FIELD): 'id', (DatasetEnricherStep.OPT_EXTRA_FIELD): 'id', (DatasetEnricherStep.OPT_MERGE_MODE): "both"],
                DatasetEnricherStep.SERVICE_DESCRIPTOR,
                context, null)

        when:
        def resultsMap = step.doExecute(inputs)
        def result = resultsMap["output"]

        then:
        result != null
        def items = result.items

        testMOValues(items)

    }

    void "merge mo datasets using uuid"() {

        DefaultCamelContext context = new DefaultCamelContext()

        Dataset ds1 = new Dataset(MoleculeObject.class, mols1())
        Dataset ds2 = new Dataset(MoleculeObject.class, mols2())
        def inputs = [(DatasetEnricherStep.VAR_INPUT): ds1, (DatasetEnricherStep.VAR_NEW_DATA): ds2]

        DatasetEnricherStep step = new DatasetEnricherStep()
        step.configure("merge mo datasets using uuid",
                [(DatasetEnricherStep.OPT_MERGE_MODE): "both"],
                DatasetEnricherStep.SERVICE_DESCRIPTOR,
                context, null
        )


        when:
        def resultsMap = step.doExecute(inputs)
        def result = resultsMap["output"]

        then:
        result != null
        def items = result.items

        testMOValues(items)

    }


    private void testMOValues(items) {

        items.size() == 3

        MoleculeObject mo1 = items[0]
        MoleculeObject mo2 = items[1]
        MoleculeObject mo3 = items[2]

        assert mo1.source == 'NNC'
        assert mo1.format == 'mol'
        assert mo1.getValue('c') == '1000'

        assert mo2.source == 'NNCC'
        assert mo2.format == 'smiles'
        assert mo2.getValue('c') == '1000'

        assert mo3.source == 'NNCCC'
        assert mo3.format == 'smiles'
        assert mo3.getValue('c') == '1000'


    }


    private void testBOValues(items) {

        items.size() == 3

        BasicObject bo1 = items[0]
        BasicObject bo2 = items[1]
        BasicObject bo3 = items[2]

        assert bo1.getValue('a') == '1'
        assert bo1.getValue('b') == '1'
        assert bo1.getValue('hello') == 'jupiter'

        assert bo2.getValue('a') == '99'
        assert bo2.getValue('b') == '99'
        assert bo2.getValue('hello') == 'saturn'
        assert bo2.getValue('foo') == 'baz'

        assert bo3.getValues().size() == 3
    }

}