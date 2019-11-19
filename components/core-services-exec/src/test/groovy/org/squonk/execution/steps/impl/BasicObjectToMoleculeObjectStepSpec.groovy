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
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class BasicObjectToMoleculeObjectStepSpec extends Specification {

    def input = [
            new BasicObject([struct: "C", num: 1, hello: 'mercury']),
            new BasicObject([struct: "CC", num: 2, hello: 'venus']),
            new BasicObject([struct: "CCC", num: 3, hello: 'world']),
    ]
    Dataset ds = new Dataset(BasicObject.class, input)

    void "simple convert"() {

        BasicObjectToMoleculeObjectStep step = new BasicObjectToMoleculeObjectStep()
        step.configure("simple convert",
                [(BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME): 'struct',
                 (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FORMAT)    : 'smiles'],
                BasicObjectToMoleculeObjectStep.SERVICE_DESCRIPTOR,
                null, null)

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", ds))
        def molsds = resultsMap["output"]

        then:
        molsds != null
        def items = molsds.items
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 2
        items[0].uuid == input[0].uuid
    }


    void "uuid and format props"() {

        BasicObjectToMoleculeObjectStep step = new BasicObjectToMoleculeObjectStep()
        step.configure("uuid and format props", [
                (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME): 'struct',
                (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FORMAT)    : 'smiles',
                (BasicObjectToMoleculeObjectStep.OPTION_PRESERVE_UUID)       : false
        ], BasicObjectToMoleculeObjectStep.SERVICE_DESCRIPTOR, null, null)

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", ds))
        def molsds = resultsMap["output"]

        then:
        molsds != null
        def items = molsds.items
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 2
        items[0].uuid != input[0].uuid
        items[0].format == 'smiles'
    }

}

