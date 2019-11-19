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

import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetWriterStepSpec extends Specification {

    void "test write mols"() {

        def mols = [
                new MoleculeObject("C", "smiles"),
                new MoleculeObject("CC", "smiles"),
                new MoleculeObject("CCC", "smiles")
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        DatasetWriterStep step = new DatasetWriterStep()
        step.configure("test write mols",
                [:],
                DatasetWriterStep.SERVICE_DESCRIPTOR,
                null, null
        )

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", ds))
        def dataset = resultsMap["output"]

        then:
        dataset != null

    }

}