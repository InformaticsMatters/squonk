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
import org.squonk.core.DockerServiceDescriptor
import org.squonk.dataset.Dataset
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.options.OptionDescriptor
import org.squonk.types.MoleculeObject
import spock.lang.Specification

import spock.lang.IgnoreIf

/**
 * Created by timbo on 13/09/16.
 */

// Alan Christie: At the moment the tests expect to write to or own
//                "/var/maven_repo". Withr Travis (CI/CD) this is not possible.
@IgnoreIf({ System.getenv('TRAVIS') != null })
class UntrustedGroovyDatasetStepSpec extends Specification {


    DefaultCamelContext context = new DefaultCamelContext()

    def createDataset() {
        def mols = [
                new MoleculeObject('C', 'smiles', [idx: 0, a: 11, b: 'red',    c: 7, d: 5]),
                new MoleculeObject('CC', 'smiles', [idx: 1, a: 23, b: 'blue',   c: 5]),
                new MoleculeObject('CCC', 'smiles', [idx: 2, a: 7,  b: 'green',  c: 5, d: 7]),
                new MoleculeObject('CCCC', 'smiles', [idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        ds.generateMetadata()
        return ds
    }

    def createStep(jobid, options) {
        DockerServiceDescriptor dsd = new DockerServiceDescriptor("id.execute.groovy", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[], [new IORoute(IORoute.Route.FILE)] as IORoute[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[], [new IORoute(IORoute.Route.FILE)] as IORoute[],
                [new OptionDescriptor(String.class, "script", "", "", OptionDescriptor.Mode.User)] as OptionDescriptor[],
                null, StepDefinitionConstants.UntrustedGroovyDatasetScript.CLASSNAME, "informaticsmatters/squonk-groovy:1.0.0",
                "execute", [:])

        UntrustedGroovyDatasetScriptStep step = new UntrustedGroovyDatasetScriptStep()
        step.configure(jobid, options, dsd, context, null)
        return step
    }

    void "simple copy dataset"() {

        Map options = ['script' :'''
def file1 = new File('input.metadata')
file1.renameTo 'output.metadata'
def file2 = new File('input.data.gz')
file2.renameTo 'output.data.gz'
''']
        String jobid = UUID.randomUUID().toString()
        UntrustedGroovyDatasetScriptStep step = createStep(jobid, options)
        Dataset input = createDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4
    }

    void "groovy consumer"() {

//        DefaultCamelContext context = new DefaultCamelContext()
        Map options = ['script' :'''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='*')
import org.squonk.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.function.Consumer

processDataset('input','output') { MoleculeObject mo ->
    mo.putValue("hello", "world")
} as Consumer
''']

        String jobid = UUID.randomUUID().toString()
        UntrustedGroovyDatasetScriptStep step = createStep(jobid, options)
        Dataset input = createDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4
        dataset.metadata.valueClassMappings['hello'] == String.class
    }


    void "groovy function"() {

//        DefaultCamelContext context = new DefaultCamelContext()
        Map options = ['script' :'''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='*')
import org.squonk.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.stream.Stream
import java.util.function.Function

processDatasetStream('input','output') { Stream<MoleculeObject> stream ->
    return stream.peek() { MoleculeObject mo ->
        mo.putValue("hello", "world")
    }
} as Function
''']

        String jobid = UUID.randomUUID().toString()
        UntrustedGroovyDatasetScriptStep step = createStep(jobid, options)
        Dataset input = createDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4
        dataset.metadata.valueClassMappings['hello'] == String.class
    }

}