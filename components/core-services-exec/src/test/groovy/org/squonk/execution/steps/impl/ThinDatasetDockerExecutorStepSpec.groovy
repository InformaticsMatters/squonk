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
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.io.InputStreamDataSource
import org.squonk.types.MoleculeObject
import org.squonk.types.SDFile
import org.squonk.types.ZipFile
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

/**
 * Created by timbo on 08/05/17.
 */
class ThinDatasetDockerExecutorStepSpec extends Specification {

    DefaultCamelContext context = new DefaultCamelContext()

    Dataset createDataset() {
        def mols = [
                new MoleculeObject('C', 'smiles', [idx: 0, a: 11, b: 'red', c: 7, d: 5]),
                new MoleculeObject('CC', 'smiles', [idx: 1, a: 23, b: 'blue', c: 5]),
                new MoleculeObject('CCC', 'smiles', [idx: 2, a: 7, b: 'green', c: 5, d: 7]),
                new MoleculeObject('CCCC', 'smiles', [idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        ds.generateMetadata()
        return ds
    }

    ZipFile createZipFile() {
        def fis = new FileInputStream("../../data/testfiles/test.zip")
        return new ZipFile(new InputStreamDataSource("zipfile", "zipfile", CommonMimeTypes.MIME_TYPE_ZIP_FILE, fis, false))
    }

    SDFile createSDFile() {
        def fis = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        return new SDFile(new InputStreamDataSource("sdf", "sdf", CommonMimeTypes.MIME_TYPE_MDL_SDF, fis, false))
    }

    def createVariableManager() {
        VariableManager varman = new VariableManager(null, 1, 1);
        return varman
    }

    def createStep(options, cmd, jobId, inputIod, outputIod) {

        DockerServiceDescriptor dsd = new DockerServiceDescriptor("id.busybox", "name", "desc", null, null, null, null, null,
                [inputIod] as IODescriptor[],
                [new IORoute(IORoute.Route.FILE)] as IORoute[],
                [outputIod] as IODescriptor[],
                [new IORoute(IORoute.Route.FILE)] as IORoute[],
                null, null, "executor", 'informaticsmatters/pipelines-busybox:1.0.0', cmd, [:])

        ThinDatasetDockerExecutorStep step = new ThinDatasetDockerExecutorStep()
        step.configure(jobId, options, dsd, context, null)
        return step
    }


    void "simple execute using dataset"() {

        Map options = ['docker.executor.id': 'id.busybox']
        DefaultDockerExecutorStep step = createStep(options,
                'cp input.data.gz output.data.gz && cp input.metadata output.metadata',
                UUID.randomUUID().toString(),
                IODescriptors.createMoleculeObjectDataset("input"),
                IODescriptors.createMoleculeObjectDataset("output"))
        Dataset input = createDataset()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def result = resultsMap["output"]

        then:
        result != null
        result.generateMetadata()
        List results = result.getItems()
        results.size() == 4
    }

    void "simple execute using zip"() {

        ZipFile input = createZipFile()
        Map args = ['docker.executor.id': 'id.busybox']
        DefaultDockerExecutorStep step = createStep(args, 'cp input.zip output.zip',
                UUID.randomUUID().toString(),
                IODescriptors.createZipFile("input"),
                IODescriptors.createZipFile("output"))

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def zip = resultsMap["output"]


        then:
        zip != null
        zip.inputStream != null

    }

    void "execute as sdf"() {

        SDFile input = createSDFile()
        Map args = ['docker.executor.id': 'id.busybox']
        DefaultDockerExecutorStep step = createStep(args, 'cp input.sdf.gz output.sdf.gz',
                UUID.randomUUID().toString(),
                IODescriptors.createSDF("input"),
                IODescriptors.createSDF("output"))

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def output = resultsMap["output"]

        then:
        output != null
        output.gunzipedInputStream.text.contains('$$$$')

    }

}
