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
 * Created by timbo on 13/09/16.
 */
class SimpleSorterStepSpec extends Specification {

    def createDataset() {
        def mols = [
                new BasicObject([idx: 0, a: 11, b: 'red',    c: 7, d: 5]),
                new BasicObject([idx: 1, a: 23, b: 'blue',   c: 5]),
                new BasicObject([idx: 2, a: 7,  b: 'green',  c: 5, d: 7]),
                new BasicObject([idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        ds.generateMetadata()
        return ds
    }

    def createStep(field, ascending, jobId, camelContext) {
        SimpleSorterStep step = new SimpleSorterStep()
        step.configure(jobId,
                [(SimpleSorterStep.OPTION_FIELD): field, (SimpleSorterStep.OPTION_ASC): ascending],
                SimpleSorterStep.SERVICE_DESCRIPTOR,
                camelContext,
                null
        )
        return step
    }

    void "invalid field name throws"() {

        DefaultCamelContext context = new DefaultCamelContext()
        SimpleSorterStep step = createStep("bananas", true, "sort tests", context)

        when:
        step.doExecute(Collections.singletonMap("input", input))

        then:
        thrown(MissingPropertyException)
    }


    void "sort tests"() {

        DefaultCamelContext context = new DefaultCamelContext()
        SimpleSorterStep step = createStep(field, asc, "sort tests", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def result = resultsMap["output"]

        expect:
        result != null
        result.generateMetadata()
        List results = result.getItems()
        results.size() == 4
        results[0].getValue('idx') == zero
        results[1].getValue('idx') == one
        results[2].getValue('idx') == two
        results[3].getValue('idx') == three


        where:
        field  | asc      | zero | one | two | three
        'a'    | true     | 2    | 0   | 3   | 1
        'a'    | false    | 1    | 3   | 0   | 2
        'b'    | true     | 1    | 2   | 3   | 0
        'b'    | false    | 0    | 3   | 2   | 1
        'c'    | true     | 3    | 1   | 2   | 0
        'd'    | true     | 3    | 0   | 2   | 1 // nulls last
        'd'    | false    | 2    | 0   | 3   | 1 // nulls last

    }
}