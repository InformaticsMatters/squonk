/*
 * Copyright (c) 2020 Informatics Matters Ltd.
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
class DatasetSplitOnNullStepSpec extends Specification {

    def createDataset() {
        def mols = [
                new BasicObject([idx: 0, a: 11, b: 'red',    c: 7, d: 5, e: "one"]),
                new BasicObject([idx: 1, a: 23, b: 'blue',   c: 5, e: "two"]),
                new BasicObject([idx: 2, a: 7,  b: 'green',  c: 5, d: 7, e: ""]),
                new BasicObject([idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        ds.generateMetadata()
        return ds
    }

    def createStep(field, jobId, camelContext) {
        DatasetSplitOnNullStep step = new DatasetSplitOnNullStep()
        step.configure(jobId,
                [(DatasetSplitOnNullStep.OPTION_FIELD): field],
                DatasetSplitOnNullStep.SERVICE_DESCRIPTOR,
                camelContext,
                null
        )
        return step
    }

    void "invalid field name throws"() {

        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitOnNullStep step = createStep("bananas", "splitonnull invalidfield", context)

        when:
        step.doExecute(Collections.singletonMap("input", input))

        then:
        thrown(MissingPropertyException)
    }


    void "split tests"() {

        when:
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitOnNullStep step = createStep("d", "splitonnull tests", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def pass = resultsMap["pass"]
        def fail = resultsMap["fail"]

        then:
        pass.items.size() == 3
        fail.items.size() == 1
    }

    void "split tests empty string"() {

        when:
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitOnNullStep step = createStep("e", "splitonnull tests", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def pass = resultsMap["pass"]
        def fail = resultsMap["fail"]

        then:
        pass.items.size() == 2
        fail.items.size() == 2
    }
}