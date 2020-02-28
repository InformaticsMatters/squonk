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
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 * Created by timbo on 13/09/16.
 */
class DefaultDockerExecutorStepSpec extends Specification {

    DefaultCamelContext context = new DefaultCamelContext()

    def createDataset() {
        def mols = [
                new MoleculeObject('C', 'smiles', [idx: 0, a: 11, b: 'red',    c: 7, d: 5]),
                new MoleculeObject('CC', 'smiles', [idx: 1, a: 23, b: 'blue',   c: 5]),
                new MoleculeObject('CCC', 'smiles', [idx: 2, a: 7,  b: 'green',  c: 5, d: 7]),
                new MoleculeObject('CCCC', 'smiles', [idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }


    def createStep(options, cmd, jobId) {
        DockerServiceDescriptor dsd = new DockerServiceDescriptor("id.busybox", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[], [new IORoute(IORoute.Route.FILE)] as IORoute[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[], [new IORoute(IORoute.Route.FILE)] as IORoute[],
                null, null, "executor", 'informaticsmatters/pipelines-busybox:1.0.0', null, cmd, [:])

        DefaultDockerExecutorStep step = new DefaultDockerExecutorStep()
        step.configure(jobId, options, dsd, context, null)
        return step
    }

    void "simple execute using json"() {

        Map args = ['docker.executor.id' :'id.busybox']
        String jobid = UUID.randomUUID().toString()
        DefaultDockerExecutorStep step = createStep(args, 'cp input.data.gz output.data.gz && cp input.metadata output.metadata', jobid)
        Dataset ds = createDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", ds))
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4

    }
}