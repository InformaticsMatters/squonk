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

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.core.DockerServiceDescriptor
import org.squonk.dataset.Dataset
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.notebook.api.VariableKey
import org.squonk.types.MoleculeObject
import org.squonk.types.SDFile
import org.squonk.types.ZipFile
import org.squonk.util.IOUtils
import spock.lang.Specification

import java.util.zip.GZIPInputStream

/**
 * Created by timbo on 08/05/17.
 */
class ThinDatasetDockerExecutorStepSpec extends Specification {

    Long producer = 1

    Dataset createDataset() {
        def mols = [
                new MoleculeObject('C', 'smiles', [idx: 0, a: 11, b: 'red', c: 7, d: 5]),
                new MoleculeObject('CC', 'smiles', [idx: 1, a: 23, b: 'blue', c: 5]),
                new MoleculeObject('CCC', 'smiles', [idx: 2, a: 7, b: 'green', c: 5, d: 7]),
                new MoleculeObject('CCCC', 'smiles', [idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }

    ZipFile createZipFile() {
        def fis = new FileInputStream("../../data/testfiles/test.zip")
        return new ZipFile(fis)
    }

    SDFile createSDFile() {
        def fis = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        return new SDFile(fis)
    }

    def createVariableManager() {
        VariableManager varman = new VariableManager(null, 1, 1);
        return varman
    }

    def createStep(args, cmd, inputRead, outputWrite,
                   inputIODescriptor, outputIODescriptor) {

        DockerServiceDescriptor dsd = new DockerServiceDescriptor("id.busybox", "name", "desc", null, null, null, null, null,
                [inputIODescriptor] as IODescriptor[], [new IORoute(IORoute.Route.FILE)] as IORoute[],
                [outputIODescriptor] as IODescriptor[], [new IORoute(IORoute.Route.FILE)] as IORoute[],
                null, null, "executor", 'busybox', cmd, [:])

        ThinDatasetDockerExecutorStep step = new ThinDatasetDockerExecutorStep()
        step.configure(producer, "job1",
                args,
                [(inputIODescriptor.name): new VariableKey(producer, inputRead)],
                [(outputIODescriptor.name): outputWrite],
                dsd
        )
        return step
    }


    void "simple execute using dataset"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        varman.putValue(
                new VariableKey(producer, "output_previous"),
                Dataset.class,
                createDataset())
        Map args = ['docker.executor.id': 'id.busybox']
        DefaultDockerExecutorStep step = createStep(args, 'cp input_d.data.gz output_d.data.gz && cp input_d.metadata output_d.metadata',
                "output_previous", "output_v",
                IODescriptors.createMoleculeObjectDataset("input_d"),
                IODescriptors.createMoleculeObjectDataset("output_d"))

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, "output_v"), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4
    }

    void "simple execute using zip"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        varman.putValue(
                new VariableKey(producer, "input_v"),
                ZipFile.class,
                createZipFile())
        Map args = ['docker.executor.id': 'id.busybox']
        DefaultDockerExecutorStep step = createStep(args, 'cp input_d.zip output_d.zip',
                "input_v", "output_v",
                IODescriptors.createZipFile("input_d"),
                IODescriptors.createZipFile("output_d"))

        when:
        step.execute(varman, context)
        ZipFile zip = varman.getValue(new VariableKey(producer, "output_v"), ZipFile.class)

        then:
        zip != null
        zip.inputStream != null

    }

    void "execute as sdf"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        varman.putValue(
                new VariableKey(producer, "input_v"),
                SDFile.class,
                createSDFile())
        Map args = ['docker.executor.id': 'id.busybox']
        DefaultDockerExecutorStep step = createStep(args, 'cp input_d.sdf.gz output_d.sdf.gz',
                "input_v", "output_v",
                IODescriptors.createSDF("input_d"),
                IODescriptors.createSDF("output_d"))

        when:
        step.execute(varman, context)
        SDFile sdf = varman.getValue(new VariableKey(producer, "output_v"), SDFile.class)

        then:
        sdf != null
        sdf.gunzipedInputStream.text.contains('$$$$')

    }

}
