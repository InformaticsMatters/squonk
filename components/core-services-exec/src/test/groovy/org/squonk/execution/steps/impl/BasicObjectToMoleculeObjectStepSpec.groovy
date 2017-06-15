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

import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
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
    Long producer = 1

    void "simple convert"() {

        VariableManager varman = new VariableManager(null, 1, 1);

        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                ds)

        BasicObjectToMoleculeObjectStep step = new BasicObjectToMoleculeObjectStep()
        step.configure(producer, "job1",
                [(BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME): 'struct'],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                ["input": new VariableKey(producer, "input")],
                [:])

        when:
        step.execute(varman, null)
        def molsds = varman.getValue(new VariableKey(producer, "output"), Dataset.class)

        then:

        molsds != null
        def items = molsds.items
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 2
        items[0].uuid == input[0].uuid

    }


    void "uuid and format props"() {

        VariableManager varman = new VariableManager(null, 1, 1);

        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                ds)

        BasicObjectToMoleculeObjectStep step = new BasicObjectToMoleculeObjectStep()
        step.configure(producer, "job1", [
                (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME): 'struct',
                (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FORMAT)    : 'smiles',
                (BasicObjectToMoleculeObjectStep.OPTION_PRESERVE_UUID)       : false
        ],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[], ["input": new VariableKey(producer, "input")],
                [:])

        when:
        step.execute(varman, null)
        def molsds = varman.getValue(new VariableKey(producer, "output"), Dataset.class)

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

