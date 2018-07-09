package org.squonk.execution

import org.squonk.core.DockerServiceDescriptor
import org.squonk.dataset.Dataset
import org.squonk.execution.ExternalExecutor
import org.squonk.execution.runners.DockerRunner
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
        DockerRunner runner = new DockerRunner("busybox", "/tmp", "/tmp",uuid)

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), null)
        exec.addData("input", new ByteArrayInputStream("Hello World!".bytes))
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
        DockerRunner runner = new DockerRunner("busybox", "/tmp", "/tmp",uuid)

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), null)
        exec.addData("input1", new ByteArrayInputStream("Hello World!".bytes))
        exec.addData("input2", new ByteArrayInputStream("Goodbye World!".bytes))
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
        DockerRunner runner = new DockerRunner("busybox", "/tmp", "/tmp",uuid)

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), null)
        exec.addData("dataset_metadata", new FileInputStream("../../data/testfiles/Kinase_inhibs.metadata"))
        exec.addData("dataset_data", new FileInputStream("../../data/testfiles/Kinase_inhibs.json.gz"))
        exec.addData("sdfile", new ByteArrayInputStream("Goodbye World!".bytes))
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
        DockerRunner runner = new DockerRunner("busybox", "/tmp", "/tmp", uuid)

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), null)
        exec.handleOutputs(sd, runner)
        exec.status = JobStatus.Status.RESULTS_READY
        def results = exec.getResults()

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
        DockerRunner runner = new DockerRunner("busybox", "/tmp", "/tmp", uuid)

        when:
        ExternalExecutor exec = new ExternalExecutor(new ExternalJobDefinition(sd, null), null)
        exec.handleOutputs(sd, runner)
        exec.status = JobStatus.Status.RESULTS_READY
        def results = exec.getResults()

        then:
        results.size() == 1
        results['output'] instanceof Dataset

        cleanup:
        dir.deleteDir()
    }

}
