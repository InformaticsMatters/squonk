/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetSelectSliceStepSpec extends Specification {

    def createDataset() {
        def mols = []
        for (i in 1..100) {
            mols << new MoleculeObject("C", "smiles", [idx:i])
        }
        println "mols starting size = " + mols.size()
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }

    def createStep(skip, count, jobId) {
        DatasetSelectSliceStep step = new DatasetSelectSliceStep()
        def opts = [:]
        if (skip != null) opts[DatasetSelectSliceStep.OPTION_SKIP] = skip
        if (count != null) opts[DatasetSelectSliceStep.OPTION_COUNT] = count
        step.configure(jobId, opts, DatasetSelectSliceStep.SERVICE_DESCRIPTOR)
        return step
    }
    
    void "test skip and count"() {
        
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSelectSliceStep step = createStep(10, 10, "test skip and count")
        Dataset input = createDataset()
        
        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input), null)
        def dataset = resultsMap["output"]
        
        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 10
        results[0].getValue("idx") == 11
    }

    void "test skip only"() {

        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSelectSliceStep step = createStep(10, null, "test skip only")
        Dataset input = createDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input), null)
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 90
        results[0].getValue("idx") == 11
    }

    void "test count only"() {

        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSelectSliceStep step = createStep(null, 10, "test count only")
        Dataset input = createDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input), null)
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 10
        results[0].getValue("idx") == 1
    }

}