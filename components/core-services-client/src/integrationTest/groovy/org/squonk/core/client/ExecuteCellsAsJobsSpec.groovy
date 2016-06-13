package org.squonk.core.client

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition
import com.im.lac.job.jobdef.JobStatus
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.notebook.api.VariableKey
import org.squonk.util.CommonMimeTypes
import spock.lang.Stepwise

import java.util.zip.GZIPInputStream

/**
 * Created by timbo on 01/06/16.
 */
@Stepwise
class ExecuteCellsAsJobsSpec extends AbstractExecuteDocker {

    void setupSpec() {
        doSetupSpec("ExecuteCellsAsJobsSpec")
    }

    void "list jobs"() {
        when:
        def jobs = jobClient.list(null)

        then:
        jobs != null
    }

    void "input loaded"() {
        when:
        int count = findResultSize(notebookId, editableId, cellId, "input")

        then:
        count == 36
    }

    void "noop cell"() {

        StepDefinition step = new StepDefinition("org.squonk.execution.steps.impl.NoopStep")
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "noop")

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "noop") == 36
    }

    void "docker cell no conversion"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.DockerProcessDataset.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "docker")
                .withOption(StepDefinitionConstants.OPTION_DOCKER_IMAGE, "busybox")
                .withOption(StepDefinitionConstants.OPTION_MEDIA_TYPE_INPUT, CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption(StepDefinitionConstants.OPTION_MEDIA_TYPE_OUTPUT, CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption(StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND,
                    "cp /source/input.meta /source/output.meta\ncp /source/input.data.gz /source/output.data.gz\n")

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "docker") == 36
    }

    void "sdf convert"() {

        StepDefinition step1 = new StepDefinition(StepDefinitionConstants.DatasetServiceExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "sdf")
                .withOption("header.Content-Encoding", "gzip")
                .withOption("header.Accept-Encoding", "gzip")
                .withOption("header.Content-Type", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption("header.Accept", CommonMimeTypes.MIME_TYPE_MDL_SDF)
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, "http://chemservices:8080/chem-services-cdk-basic/rest/v1/converters/convert_to_sdf")


        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step1)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        InputStream is = notebookClient.readStreamValue(notebookId, editableId, cellId, "sdf")
        is = new GZIPInputStream(is)
        def sdf = is.text
        def parts = sdf.split("END")

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        parts.length == 37

    }

    void "docker cell sdf conversion"() {

        StepDefinition step1 = new StepDefinition(StepDefinitionConstants.DatasetServiceExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "_docker_input")
                .withOption("header.Content-Encoding", "gzip")
                .withOption("header.Accept-Encoding", "gzip")
                .withOption("header.Content-Type", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption("header.Accept", CommonMimeTypes.MIME_TYPE_MDL_SDF)
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, "http://chemservices:8080/chem-services-cdk-basic/rest/v1/converters/convert_to_sdf")

        StepDefinition step2 = new StepDefinition(StepDefinitionConstants.DockerProcessDataset.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "_docker_input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "_docker_output")
                .withOption(StepDefinitionConstants.OPTION_DOCKER_IMAGE, "busybox")
                .withOption(StepDefinitionConstants.OPTION_MEDIA_TYPE_INPUT, CommonMimeTypes.MIME_TYPE_MDL_SDF)
                .withOption(StepDefinitionConstants.OPTION_MEDIA_TYPE_OUTPUT, CommonMimeTypes.MIME_TYPE_MDL_SDF)
                .withOption(StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND,
                "cp /source/input.sdf.gz /source/output.sdf.gz\n")

        StepDefinition step3 = new StepDefinition(StepDefinitionConstants.SdfUpload.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, new VariableKey(cellId, "_docker_output"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "docker");

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step1, step2, step3)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "docker") == 36
    }

    void "groovy in docker cell noop"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.UnrustedGroovyDataset.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "groovy-noop")
                .withOption(StepDefinitionConstants.UnrustedGroovyDataset.OPTION_SCRIPT,
                '''
def file1 = new File('/source/input.meta')
file1.renameTo '/source/output.meta'
def file2 = new File('/source/input.data.gz')
file2.renameTo '/source/output.data.gz'
''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-noop") == 36
    }

    void "groovy in docker cell using consumer"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.UnrustedGroovyDataset.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "groovy-api-consumer")
                .withOption(StepDefinitionConstants.UnrustedGroovyDataset.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import com.im.lac.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.function.Consumer

processDataset('/source/input','/source/output') { MoleculeObject mo ->
    mo.putValue("hello", "world")
} as Consumer

''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-api-consumer") == 36
    }

    void "groovy in docker cell using function"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.UnrustedGroovyDataset.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "groovy-api-function")
                .withOption(StepDefinitionConstants.UnrustedGroovyDataset.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import com.im.lac.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.stream.Stream
import java.util.function.Function

processDatasetStream('/source/input','/source/output') { Stream<MoleculeObject> stream ->
    return stream.peek() { MoleculeObject mo ->
        mo.putValue("hello", "world")
    }
} as Function
''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-api-function") == 36
    }

    void "groovy in docker cell using strong typing"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.UnrustedGroovyDataset.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "groovy-api-strong")
                .withOption(StepDefinitionConstants.UnrustedGroovyDataset.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import org.squonk.dataset.Dataset
import com.im.lac.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.stream.Stream

Dataset dataset = readDatasetFromFiles('/source/input')
Stream<MoleculeObject> stream = dataset.stream.peek() { MoleculeObject mo ->
    mo.putValue("hello", "world")
}
dataset.replaceStream(stream)
writeDatasetToFiles(dataset, '/source/output', true)

''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-api-strong") == 36
    }


    void "groovy in docker cell using weak typing"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.UnrustedGroovyDataset.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "groovy-api-weak")
                .withOption(StepDefinitionConstants.UnrustedGroovyDataset.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import static org.squonk.util.MoleculeObjectUtils.*

def dataset = readDatasetFromFiles('/source/input')
def stream = dataset.stream.peek() { mo ->
    mo.putValue("hello", "world")
}
dataset.replaceStream(stream)
writeDatasetToFiles(dataset, '/source/output', true)

''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-api-weak") == 36
    }

}
