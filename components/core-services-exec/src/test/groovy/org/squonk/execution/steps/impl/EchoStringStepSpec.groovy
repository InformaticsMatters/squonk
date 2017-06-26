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


import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 * Created by timbo on 06/01/16.
 */
class EchoStringStepSpec extends Specification {

    void "simple test"() {
        String value = "hello"
        VariableManager varman = new VariableManager(null,1,1);
        Long producer = 1
        varman.putValue(
                new VariableKey(producer, "input"),
                String.class,
                value)

        EchoStringStep step = new EchoStringStep()
        step.configure(producer, "job1",
                [:],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                ["input":new VariableKey(producer, "input")],
                ["output":"output"])

        when:
        step.execute(varman, null)
        String result = varman.getValue(new VariableKey(producer, "output"), String.class)

        then:
        result == value
    }
}
