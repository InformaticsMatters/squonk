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

import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CSVReaderStepSpec extends Specification {

    static String CSV1 = '''\
field1,field2,field3
1,one,uno
2,two,duo
3,three,tres'''

    static String TAB1 = '''\
field1\tfield2\tfield3
1\tone\tuno
2\ttwo\tduo
3\tthree\ttres'''

    Long producer = 1

    void "simple csv reader with header"() {
        //println "simple csv reader with header"
        InputStream is = new ByteArrayInputStream(CSV1.bytes)
        VariableManager varman = new VariableManager(null, 1, 1);

        varman.putValue(
                new VariableKey(producer, "input"),
                InputStream.class,
                is)
        varman.putValue(
                new VariableKey(producer, "input"),
                String.class,
                "some filename")


        CSVReaderStep step = new CSVReaderStep()
        step.configure(producer, "job1", [
                (CSVReaderStep.OPTION_FORMAT_TYPE)               : 'DEFAULT',
                (CSVReaderStep.OPTION_USE_HEADER_FOR_FIELD_NAMES): true,
                (CSVReaderStep.OPTION_SKIP_HEADER_LINE)          : true
        ],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[], [(CSVReaderStep.VAR_CSV_INPUT): new VariableKey(producer, "input")], [:]
        )

        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, CSVReaderStep.VAR_DATASET_OUTPUT), Dataset.class)

        then:
        dataset != null
        def items = dataset.items
        items.size() == 3
        items[0].values.size() == 3
        dataset.metadata.getProperties()['source'].contains("some filename")
    }

    void "simple tab reader without header"() {
        //println "simple tab reader without header"
        InputStream is = new ByteArrayInputStream(TAB1.bytes)
        VariableManager varman = new VariableManager(null, 1, 1);
        varman.putValue(
                new VariableKey(producer, "input"),
                InputStream.class,
                is)


        CSVReaderStep step = new CSVReaderStep()

        step.configure(producer, "job1",
                [(CSVReaderStep.OPTION_FORMAT_TYPE): 'TDF'],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                [(CSVReaderStep.VAR_CSV_INPUT): new VariableKey(producer, "input")],
                [:])

        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, CSVReaderStep.VAR_DATASET_OUTPUT), Dataset.class)

        then:
        dataset != null
        def items = dataset.items
        items.size() == 4
        items[0].values.size() == 3
    }

}

