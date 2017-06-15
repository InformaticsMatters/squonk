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

package org.squonk.execution.steps.impl

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.notebook.api.VariableKey
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 * Created by timbo on 13/09/16.
 */
class DatasetSorterStepSpec extends Specification {


    Long producer = 1

    def createDataset() {
        def mols = [
                new BasicObject([idx: 0, a: 11, b: 'red',    c: 7, d: 5]),
                new BasicObject([idx: 1, a: 23, b: 'blue',   c: 5]),
                new BasicObject([idx: 2, a: 7,  b: 'green',  c: 5, d: 7]),
                new BasicObject([idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }

    def createVariableManager() {
        VariableManager varman = new VariableManager(null, 1, 1);
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                createDataset())
        return varman
    }

    def createStep(expression) {
        DatasetSorterStep step = new DatasetSorterStep()
        step.configure(producer, "job1",
                [(DatasetSorterStep.OPTION_DIRECTIVES): expression],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                [(DatasetSorterStep.VAR_INPUT_DATASET): new VariableKey(producer, "input")],
                [:]
        )
        return step
    }

    void "parse single"() {

        DatasetMetadata meta = new DatasetMetadata(BasicObject.class, [foo: String.class, bar: String.class])
        DatasetSorterStep step = new DatasetSorterStep()
        def directives = step.parse(expression, meta)

        expect:
        directives.size() == 1
        directives[0].field == field
        directives[0].ascending == asc

        where:
        expression | field | asc
        'foo ASC'  | 'foo' | true
        'foo asc'  | 'foo' | true
        'foo'      | 'foo' | true
        'foo DESC' | 'foo' | false
        'foo desc' | 'foo' | false
    }


    void "invalid field name throws"() {

        DatasetMetadata meta = new DatasetMetadata(BasicObject.class, [foo: String.class, bar: String.class])
        DatasetSorterStep step = new DatasetSorterStep()

        when:
        def directives = step.parse('invalidfield', meta)

        then:
        thrown(IllegalArgumentException)

    }

    void "invalid directive throws"() {

        DatasetMetadata meta = new DatasetMetadata(BasicObject.class, [foo: String.class, bar: String.class])
        DatasetSorterStep step = new DatasetSorterStep()

        when:
        def directives = step.parse('foo UPWARDS', meta)

        then:
        thrown(IllegalArgumentException)

    }

    void "extra tokens throws"() {

        DatasetMetadata meta = new DatasetMetadata(BasicObject.class, [foo: String.class, bar: String.class])
        DatasetSorterStep step = new DatasetSorterStep()

        when:
        def directives = step.parse('foo ASC faster', meta)

        then:
        thrown(IllegalArgumentException)

    }

    void "not comparable throws"() {

        DatasetMetadata meta = new DatasetMetadata(BasicObject.class, [foo: Object.class])
        DatasetSorterStep step = new DatasetSorterStep()

        when:
        def directives = step.parse('foo ASC', meta)

        then:
        IllegalArgumentException e = thrown()
        e.getMessage() == 'Field type is not Comparable: java.lang.Object'

    }


    void "sort tests"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        DatasetSorterStep step = createStep(expression)

        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, ValueTransformerStep.VAR_OUTPUT_DATASET), Dataset.class)

        expect:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4
        results[0].getValue('idx') == zero
        results[1].getValue('idx') == one
        results[2].getValue('idx') == two
        results[3].getValue('idx') == three


        where:
        expression        | zero | one | two | three
        'a ASC'           | 2    | 0   | 3   | 1
        'a DESC'          | 1    | 3   | 0   | 2
        'b ASC'           | 1    | 2   | 3   | 0
        'b DESC'          | 0    | 3   | 2   | 1
        'c ASC'           | 3    | 1   | 2   | 0
        'c ASC\na ASC'    | 3    | 2   | 1   | 0 // newline as seprator
        'c ASC, a ASC'    | 3    | 2   | 1   | 0 // comma as separator
        'd ASC'           | 3    | 0   | 2   | 1 // nulls last
        'd DESC'          | 2    | 0   | 3   | 1 // nulls last

    }
}