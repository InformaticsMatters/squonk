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
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.types.MoleculeObject
import spock.lang.Ignore
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class FragnetExpansionStepSpec extends Specification {

    def createNciDataset() {
        return Molecules.nci10Dataset()
    }

    def createDuplicatesDataset() {
        def mols = [
                new MoleculeObject("c1ccccc1Cl", "smiles"),
                new MoleculeObject("c1ccccc1Br", "smiles")
        ]
        return new MoleculeObjectDataset(mols).dataset
    }

    def createStep(hops, hac, rac, jobId) {
        FragnetExpansionStep step = new FragnetExpansionStep()
        def opts = [:]
        opts[FragnetExpansionStep.OPTION_HOPS] = hops
        opts[FragnetExpansionStep.OPTION_HAC] = hac
        opts[FragnetExpansionStep.OPTION_RAC] = rac

        step.configure(jobId, opts, FragnetExpansionStep.SERVICE_DESCRIPTOR)
        return step
    }

    // These tests require a running fragent search service so are @Ignored for now.

    @Ignore
    void "test 1 hop"() {
        
        def context = new DefaultCamelContext()
        def step = createStep(1, 3, 1, "test 1 hop")
        def input = createNciDataset()
        
        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input), context)
        def dataset = resultsMap["output"]
        
        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() > 0
        results[0].getValue("src_mols").size() > 0
    }

    @Ignore
    void "test duplicate"() {

        def context = new DefaultCamelContext()
        def step = createStep(1, 1, 0, "test duplicate")
        def input = createDuplicatesDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input), context)
        def dataset = resultsMap["output"]
        dataset.generateMetadata()
        List results = dataset.getItems()
        int count = 0
        results.each {
            count += it.getValue("src_mols").size()
        }
        println "${results.size()} $count"

        then:
        results.size() > 0
        results[0].getValue("src_mols").size() > 0
        count > results.size()
    }

}