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
class DatasetMoleculesFromFieldStepSpec extends Specification {

    def context = new DefaultCamelContext()

    def createDataset() {
        def bos = []
        int count = 1
        for (i in 1..10) {
            BasicObject bo = new BasicObject();
            if (i % 2 == 0) {
                MoleculeObject[] mols = new MoleculeObject[5]
                for (j in 0..4) {
                    mols[j] = new MoleculeObject("C" * count++, "smiles", [idx: count])
                }
                bo.putValue("mols", mols)
            }
            bos << bo
        }
        println "bos starting size = " + bos.size()
        Dataset ds = new Dataset(BasicObject.class, bos)
        return ds
    }

    def createStep(field, jobId) {
        DatasetMoleculesFromFieldStep step = new DatasetMoleculesFromFieldStep()
        step.configure(jobId,
                [(DatasetMoleculesFromFieldStep.OPTION_MOLECULES_FIELD): field],
                DatasetMoleculesFromFieldStep.SERVICE_DESCRIPTOR, context, null)
        return step
    }

    void "get 25 mols"() {

        def step = createStep("mols", "get 25 mols")
        def input = createDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.generateMetadata()
        def results = dataset.getItems()
        results.size() == 25
        dataset.metadata.size == 25
        results[24].source.length() == 25
    }

}