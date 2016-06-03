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
class ExecuteServicesAsJobsSpec extends Specification {

    static String username = 'squonkuser'
    static SquonkClientConfig CONFIG = new SquonkClientConfig()
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
        sleep(10000) // need to wait for everything to start
        NotebookDTO notebookDTO = notebookClient.createNotebook(username, "JobStatusRestClientSpec name", "JobStatusRestClientSpec description")
        editable = notebookClient.listEditables(notebookDTO.getId(), username)[0]
        DatasetHandler dh = new DatasetHandler(MoleculeObject.class)
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        VariableWriteContext vhcontext = new VariableWriteContext(notebookClient, editable.notebookId, editable.id, 1, "input")
        dh.writeVariable(dataset, vhcontext)
        notebookId = editable.notebookId
        editableId = editable.id
    }

    void cleanupSpec() {
        //sleep(2000) // wait for everything to finish before we delete
        notebookClient.deleteNotebook(editable.getNotebookId())
    }

    int findResultSize(notebookId, editableId, cellId, varname) {
        def metaJson = notebookClient.readTextValue(notebookId, editableId, cellId, varname)
        return metaJson ? JSON.objectFromJson(metaJson, DatasetMetadata.class).size : -1
    }

    JobStatus waitForJob(def jobId) {
        JobStatus status
        for (int i=0; i<20; i++) {
            sleep(500)
            status = jobClient.get(jobId)
            if (status.status == JobStatus.Status.COMPLETED) {
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
        jobs.size() == 0 // initially no jobs
    }

    void "cdk logp"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "cdklogp")
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, CONFIG.basicCdkChemServicesBaseUrl+"/calculators/logp")

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)
        int count = findResultSize(notebookId, editableId, cellId, "cdklogp")

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        count == 36
    }

    void "chemaxon logp"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "cxnlogp")
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, CONFIG.basicChemaxonChemServicesBaseUrl+"/calculators/logp")


        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)
        int count = findResultSize(notebookId, editableId, cellId, "cxnlogp")

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        count == 36
    }

    void "chemaxon spherex"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "spherex")
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, CONFIG.basicChemaxonChemServicesBaseUrl+"/descriptors/clustering/spherex/ecfp4")
                .withOption('header.min_clusters', 3)
                .withOption('header.max_clusters', 6)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)
        int count = findResultSize(notebookId, editableId, cellId, "spherex")

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        count == 36
    }

    void "chemaxon screen"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "screen")
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, CONFIG.basicChemaxonChemServicesBaseUrl+"/descriptors/screening/ecfp4")
                .withOption('option.filter', true)
                .withOption('header.threshold', 0.5)
                .withOption('header.structure_source', 'Oc1cccc(c1)c2nc(N3CCOCC3)c4oc5ncccc5c4n2')
                .withOption('header.structure_format', 'smiles')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)
        int count = findResultSize(notebookId, editableId, cellId, "screen")

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        count >= 1
        count < 36
    }
}
