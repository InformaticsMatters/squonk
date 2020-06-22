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
class DatasetSplitStepSpec extends Specification {

    def createDataset() {
        Random g = new Random()
        def mols = []
        for (int i=0; i<100; i++) {
            mols.add(new BasicObject([idx: 1, a: g.nextInt(100), b: g.nextInt(1000)]))
        }

        Dataset ds = new Dataset(BasicObject.class, mols)
        ds.generateMetadata()
        return ds
    }

    def createStep(frac, rand, jobId, camelContext) {
        DatasetSplitStep step = new DatasetSplitStep()
        step.configure(jobId,
                [(DatasetSplitStep.OPTION_FRAC): frac, (DatasetSplitStep.OPTION_RANDOMISE): rand],
                DatasetSplitStep.SERVICE_DESCRIPTOR,
                camelContext,
                null
        )
        return step
    }


    void "split count sequential"() {

        when:
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitStep step = createStep(20, false, "split count sequential", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def pass = resultsMap["pass"]
        def fail = resultsMap["fail"]

        then:
        pass.items.size() == 20
        fail.items.size() == 80
    }

    void "split count random"() {

        when:
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitStep step = createStep(20, true, "split count sequential", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def pass = resultsMap["pass"]
        def fail = resultsMap["fail"]

        then:
        pass.items.size() == 20
        fail.items.size() == 80
    }

    void "split frac sequential"() {

        when:
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitStep step = createStep(0.2, false, "split frac sequential", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def pass = resultsMap["pass"]
        def fail = resultsMap["fail"]

        then:
        pass.items.size() == 20
        fail.items.size() == 80
    }

    void "split frac random"() {

        when:
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitStep step = createStep(0.2, true, "split count sequential", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def pass = resultsMap["pass"]
        def fail = resultsMap["fail"]

        then:
        pass.items.size() == 20
        fail.items.size() == 80
    }
}