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

package org.squonk.execution.steps

import org.squonk.io.IODescriptor
import org.squonk.notebook.api.VariableKey
import org.squonk.options.types.Structure
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 01/02/16.
 */
class StepDefintionSpec extends Specification {

    void "test to/from json"() {

        StepDefinition step1 = new StepDefinition('foo.bar.Baz')
                .withOptions(['hello':'world'])
                .withInputs([new IODescriptor("in", "text/plain", String.class, null)] as IODescriptor[])
                .withOutputs([new IODescriptor("out", "text/plain", String.class, null)] as IODescriptor[])
                .withInputVariableMappings(["in": new VariableKey(1,'input')])
                .withOutputVariableMappings(["out":'output'])

        when:
        String json = JsonHandler.getInstance().objectToJson(step1)
        StepDefinition step2 = JsonHandler.getInstance().objectFromJson(json, StepDefinition.class)

        then:
        step2.getOptions().size() == 1
        step2.getInputVariableMappings().size() == 1
        step2.getOutputVariableMappings().size() == 1
        step2.getImplementationClass() == 'foo.bar.Baz'
    }

    void "polymorphic map"() {
        StepDefinition step1 = new StepDefinition('foo.bar.Baz')
                .withOption('string', 'world')
                .withOption('integer', new Integer(99))
                .withOption("date", new Date())
                .withOption("bigint", new BigInteger(999))
                .withOption("double", new Double(999.9))
                .withOption("float", new Float(999.9))
                .withOption("simplemol", new Structure('C', 'smiles'))
                //.withOption("mo", new MoleculeObject('C', 'smiles'))
                .withInputs([new IODescriptor("in", "text/plain", String.class, null)] as IODescriptor[])
                .withOutputs([new IODescriptor("out", "text/plain", String.class, null)] as IODescriptor[])
                .withInputVariableMappings(["in":new VariableKey(1,'input')])
                .withOutputVariableMappings(["out":'output'])

        when:
        String json = JsonHandler.getInstance().objectToJson(step1)
        println json
        StepDefinition step2 = JsonHandler.getInstance().objectFromJson(json, StepDefinition.class)

        then:
        step2.options['string'].getClass() == String.class
        step2.options['integer'].getClass() == Integer.class
        step2.options['date'].getClass() == Date.class
        step2.options['bigint'].getClass() == BigInteger.class
        step2.options['double'].getClass() == Double.class
        step2.options['float'].getClass() == Float.class
        step2.options['simplemol'].getClass() == Structure.class
//        step2.options['mo'].getClass() == MoleculeObject.class

    }

}
