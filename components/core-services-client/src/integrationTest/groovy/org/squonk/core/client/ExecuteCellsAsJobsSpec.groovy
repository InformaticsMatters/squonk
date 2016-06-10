package org.squonk.core.client

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition
import com.im.lac.job.jobdef.JobStatus
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition
import com.im.lac.types.MoleculeObject
import org.squonk.core.client.config.SquonkClientConfig
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.execution.variable.impl.VariableWriteContext
import org.squonk.notebook.api.NotebookDTO
import org.squonk.notebook.api.NotebookEditableDTO
import org.squonk.notebook.api.VariableKey
import org.squonk.types.DatasetHandler
import org.squonk.types.io.JsonHandler
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 01/06/16.
 */
@Stepwise
class ExecuteCellsAsJobsSpec extends Specification {

    static String username = 'squonkuser'
    static JsonHandler JSON = JsonHandler.instance

    @Shared
    JobStatusRestClient jobClient = new JobStatusRestClient()
    @Shared
    NotebookRestClient notebookClient = new NotebookRestClient()
    @Shared
    NotebookEditableDTO editable
    @Shared
    Long notebookId
    @Shared
    Long editableId
    @Shared
    Long cellId = 1

    void setupSpec() {
        NotebookDTO notebookDTO = notebookClient.createNotebook(username, "ExecuteCellsAsJobsSpec name", "ExecuteCellsAsJobsSpec description")
        editable = notebookClient.listEditables(notebookDTO.getId(), username)[0]
        DatasetHandler dh = new DatasetHandler(MoleculeObject.class)
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        VariableWriteContext vhcontext = new VariableWriteContext(notebookClient, editable.notebookId, editable.id, 1, "input")
        dh.writeVariable(dataset, vhcontext)
        notebookId = editable.notebookId
        editableId = editable.id
    }

    void cleanupSpec() {
        notebookClient.deleteNotebook(editable.getNotebookId())
    }

    int findResultSize(notebookId, editableId, cellId, varname) {
        def metaJson = notebookClient.readTextValue(notebookId, editableId, cellId, varname)
        return metaJson ? JSON.objectFromJson(metaJson, DatasetMetadata.class).size : -1
    }

    DatasetMetadata readMetadata(notebookId, editableId, cellId, varname) {
        def metaJson = notebookClient.readTextValue(notebookId, editableId, cellId, varname)
        return metaJson ? JSON.objectFromJson(metaJson, DatasetMetadata.class) : null
    }

    JobStatus waitForJob(def jobId) {
        JobStatus status
        for (int i=0; i<20; i++) {
            sleep(500)
            status = jobClient.get(jobId)
            if (status.status == JobStatus.Status.COMPLETED || status.status == JobStatus.Status.ERROR) {
                //println "job completed"
                break
            }
            //println "trying again ..."
        }
        return status
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

    void "docker cell"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.DockerProcessDataset.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "docker")
                .withOption(StepDefinitionConstants.OPTION_DOCKER_IMAGE, "busybox")
                .withOption(StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND,
                    "mv /source/input.meta /source/output.meta\nmv /source/input.data.gz /source/output.data.gz\n")

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

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
import org.squonk.dataset.Dataset
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
