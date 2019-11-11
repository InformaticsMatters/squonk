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
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetSelectRandomStepSpec extends Specification {

    DefaultCamelContext context = new DefaultCamelContext()

    def createDataset() {
        def mols = []
        for (i in 1..100) {
            mols << new MoleculeObject("C", "smiles", [idx:i])
        }
        println "mols starting size = " + mols.size()
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }

    def createStep(random, count, jobId) {
        DatasetSelectRandomStep step = new DatasetSelectRandomStep()
        def opts = [:]
        if (random != null) opts[DatasetSelectRandomStep.OPTION_RANDOM] = random
        if (count != null) opts[DatasetSelectRandomStep.OPTION_COUNT] = count
        step.configure(jobId, opts, DatasetSelectRandomStep.SERVICE_DESCRIPTOR, context, null)
        return step
    }
    
    void "test random and count"() {

        DatasetSelectRandomStep step = createStep(0.2f, 10, "test random and count")
        Dataset input = createDataset()
        
        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def dataset = resultsMap["output"]
        
        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() <= 10
    }

}