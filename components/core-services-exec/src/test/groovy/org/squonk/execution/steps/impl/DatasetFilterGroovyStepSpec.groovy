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
import groovy.transform.Canonical
import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 * Created by timbo on 19/03/2016.
 */
class DatasetFilterGroovyStepSpec extends Specification {


    static def values = [
            new Data(1, 1.1, "one"),
            new Data(2, 2.2, "two"),
            new Data(3, 3.3, "three"),
            new Data(4, 4.4, "four"),
            new Data(5, 5.5, "five"),
            new Data(6, 6.6, "six")
    ]


    void "run script with GrooovyShell"() {


        def script = '''
val.with {
    i < 5 && f > 2.0
}
'''
        def binding = new Binding()
        def shell = new GroovyShell(binding)

        when:
        def result = values.findAll() {
            binding.setProperty("val", it)
            shell.evaluate(script)
        }

        then:
        result.size() == 3
    }

    void "run script with GroovyClassLoader"() {

        String filter = 'i < 5 && f > 2.0'

        def gcl = new GroovyClassLoader()
        def cls = gcl.parseClass('''
class Filter {
    boolean run(def val) {
        val.with {
''' + filter + '''
        }
    }
}''')
        def instance = cls.newInstance()

        when:
        def result = values.findAll() {
            instance.run(it)
        }

        then:
        result.size() == 3
    }

    @Canonical
    static class Data {
        int i
        float f
        String s
    }


    def input = [
            new BasicObject([i:1, f:1.1f, s:'one']),
            new BasicObject([i:2, f:2.2f, s:'two']),
            new BasicObject([i:3, f:3.3f, s:'three']),
            new BasicObject([i:4, f:4.4f, s:'four']),
            new BasicObject([i:5, f:5.5f, s:'five']),
            new BasicObject([i:6, f:6.6f, s:'six']),
    ]
    Dataset ds = new Dataset(BasicObject.class, input)
    Long producer = 1

    void "simple filter step"() {

        VariableManager varman = new VariableManager(null, 1, 1);

        varman.putValue(
                new VariableKey(producer,"input"),
                Dataset.class,
                ds)

        DatasetFilterGroovyStep step = new DatasetFilterGroovyStep()
        step.configure(producer, "job1",
                [(DatasetFilterGroovyStep.OPTION_SCRIPT):'i < 5 && f > 2.0'],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                ["input":new VariableKey(producer, "input")],
                [:]
        )

        when:
        step.execute(varman, null)
        Dataset output = varman.getValue(new VariableKey(producer, "output"), Dataset.class)

        then:

        output != null
        output.items.size() == 3
    }

}
