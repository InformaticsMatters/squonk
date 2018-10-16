/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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
package org.squonk.execution

import org.squonk.core.DockerServiceDescriptor
import org.squonk.core.ServiceDescriptor
import org.squonk.dataset.Dataset
import org.squonk.execution.runners.DockerRunner
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.execution.steps.impl.DatasetSelectSliceStep
import org.squonk.io.IODescriptor
import org.squonk.jobdef.ExternalJobDefinition
import org.squonk.jobdef.JobStatus
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.squonk.types.SDFile
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

import java.nio.file.Files

class ExternalExecutorSpec extends Specification {

    void "write single variable"() {

        def uuid = UUID.randomUUID().toString()
        def dir = new File("/tmp/" + uuid)
        dir.createDirIfNotExists()
        def inputiods = [new IODescriptor("input", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]
        DockerServiceDescriptor sd = new DockerServiceDescriptor(uuid, "name", inputiods, null)
        DockerRunner runner = new DockerRunner("busybox", "/tmp",uuid)
        def data = ["input": new SDFile(new File("../../data/testfiles/Kinase_inhibs.sdf.gz"), true)]

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), data)
        exec.handleInputs(sd, runner)

        then:
        new File(dir, "input.sdf.gz").exists()

        cleanup:
        dir.deleteDir()
    }

    void "write two variables"() {

        def uuid = UUID.randomUUID().toString()
        def dir = new File("/tmp/" + uuid)
        println "Working in /tmp/$uuid"
        dir.createDirIfNotExists()
        def inputiods = [
                new IODescriptor("input1", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class),
                new IODescriptor("input2", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)
        ] as IODescriptor[]
        DockerServiceDescriptor sd = new DockerServiceDescriptor(uuid, "name", inputiods, null)
        DockerRunner runner = new DockerRunner("busybox", "/tmp", uuid)
        def data = [
                "input1": new SDFile(new ByteArrayInputStream("Hello World!".bytes), false),
                "input2": new SDFile(new ByteArrayInputStream("Goodbye World!".bytes), false)
        ]

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), data)
        exec.handleInputs(sd, runner)

        then:
        new File(dir, "input1.sdf.gz").exists()
        new File(dir, "input2.sdf.gz").exists()

        cleanup:
        dir.deleteDir()
    }

    void "write complex variables"() {

        def uuid = UUID.randomUUID().toString()
        def dir = new File("/tmp/" + uuid)
        dir.createDirIfNotExists()
        def inputiods = [
                new IODescriptor("dataset", CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON, Dataset.class, BasicObject.class),
                new IODescriptor("sdfile", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)
        ] as IODescriptor[]
        DockerServiceDescriptor sd = new DockerServiceDescriptor(uuid, "name", inputiods, null)
        DockerRunner runner = new DockerRunner("busybox", "/tmp", uuid)
        Dataset ds = new Dataset(
                new FileInputStream("../../data/testfiles/Kinase_inhibs.json.gz"),
                new FileInputStream("../../data/testfiles/Kinase_inhibs.metadata"))
        SDFile sdf = new SDFile(new ByteArrayInputStream("Goodbye World!".bytes), false)
        def data = ["dataset": ds, "sdfile": sdf]

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), data)
        exec.handleInputs(sd, runner)

        then:
        new File(dir, "sdfile.sdf.gz").exists()
        new File(dir, "dataset.data.gz").exists()
        new File(dir, "dataset.metadata").exists()

        cleanup:
        dir.deleteDir()
    }

    void "read single variable"() {

        def uuid = UUID.randomUUID().toString()
        def dir = new File("/tmp/" + uuid)
        dir.createDirIfNotExists()
        def source = new File("../../data/testfiles/Building_blocks_GBP.sdf.gz")
        def dest = new File(dir, "output.sdf.gz")
        Files.copy(source.toPath(), dest.toPath())
        def outputiods = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]
        DockerServiceDescriptor sd = new DockerServiceDescriptor(uuid, "name", null, outputiods)
        DockerRunner runner = new DockerRunner("busybox", "/tmp", uuid)

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), [:])
        exec.handleOutputs(sd, runner.getHostWorkDir())
        exec.status = JobStatus.Status.RESULTS_READY
        def results = exec.getResultsAsObjects()

        then:
        results.size() == 1
        results['output'] instanceof SDFile

        cleanup:
        dir.deleteDir()
    }

    void "read dataset"() {

        def uuid = UUID.randomUUID().toString()
        def dir = new File("/tmp/" + uuid)
        dir.createDirIfNotExists()
        println "Working in $dir"
        def source = new File("../../data/testfiles/Building_blocks_GBP.data.gz")
        def dest = new File(dir, "output.data.gz")
        Files.copy(source.toPath(), dest.toPath())
        source = new File("../../data/testfiles/Building_blocks_GBP.metadata")
        dest = new File(dir, "output.metadata")
        Files.copy(source.toPath(), dest.toPath())

        def outputiods = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class)] as IODescriptor[]
        DockerServiceDescriptor sd = new DockerServiceDescriptor(uuid, "name", null, outputiods)
        DockerRunner runner = new DockerRunner("busybox", "/tmp", uuid)

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), [:])
        exec.handleOutputs(sd, runner.getHostWorkDir())
        exec.status = JobStatus.Status.RESULTS_READY
        def results = exec.getResultsAsObjects()

        then:
        results.size() == 1
        results['output'] instanceof Dataset

        cleanup:
        dir.deleteDir()
    }

    void "AbstractDatasetStep"() {

        ServiceDescriptor sd = DatasetSelectSliceStep.SERVICE_DESCRIPTOR
        Dataset ds = new Dataset(
                new FileInputStream("../../data/testfiles/Kinase_inhibs.json.gz"),
                new FileInputStream("../../data/testfiles/Kinase_inhibs.metadata"))
        def options = [
                (StepDefinitionConstants.DatasetSelectSlice.OPTION_SKIP): 5,
                (StepDefinitionConstants.DatasetSelectSlice.OPTION_COUNT): 10
        ]

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, options), ['input':ds])
        exec.execute()
        def results = exec.getResultsAsObjects()

        then:
        results.size() == 1
        Dataset ds2 = results.values().iterator().next()
        ds2.items.size() == 10

    }

}
