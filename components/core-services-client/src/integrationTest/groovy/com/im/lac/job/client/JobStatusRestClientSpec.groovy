package com.im.lac.job.client

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition
import com.im.lac.job.jobdef.JobStatus
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition
import com.im.lac.types.MoleculeObject
import org.squonk.core.client.NotebookRestClient
import org.squonk.core.client.config.SquonkClientConfig
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.execution.variable.impl.VariableWriteContext
import org.squonk.notebook.api.NotebookDTO
import org.squonk.notebook.api.NotebookEditableDTO
import org.squonk.notebook.api.VariableKey
import org.squonk.types.DatasetHandler
import org.squonk.types.io.JsonHandler
import org.squonk.util.IOUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 01/06/16.
 */
@Stepwise
class JobStatusRestClientSpec extends Specification {

    static String username = 'squonkuser'
    static SquonkClientConfig CONFIG = new SquonkClientConfig()

    @Shared JobStatusRestClient jobClient = new JobStatusRestClient()
    @Shared NotebookRestClient notebookClient = new NotebookRestClient()
    @Shared NotebookEditableDTO editable

    void setupSpec() {
        sleep(10000) // need to wait for everything to start
        NotebookDTO notebookDTO = notebookClient.createNotebook(username, "JobStatusRestClientSpec name", "JobStatusRestClientSpec description")
        editable = notebookClient.listEditables(notebookDTO.getId(), username)[0]
        println "created editable $editable.id"
        DatasetHandler dh = new DatasetHandler(MoleculeObject.class)
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        VariableWriteContext vhcontext = new VariableWriteContext(notebookClient, editable.notebookId, editable.id, 1, "input")
        dh.writeVariable(dataset, vhcontext)

    }

    void cleanupSpec() {
        //sleep(2000) // wait for everything to finish before we delete
        notebookClient.deleteNotebook(editable.getNotebookId())
    }

    void "list jobs"() {
        when:
        def jobs = jobClient.list(null)

        then:
        jobs != null
        jobs.size() == 0 // initially no jobs
    }


    JobStatus waitForJob(def jobId) {
        JobStatus status
        for (int i=0; i<20; i++) {
            sleep(500)
            status = jobClient.get(jobId)
            if (status.status == JobStatus.Status.COMPLETED) {
                println "job completed"
                break
            }
            //println "trying again ..."
        }
        return status
    }

    void "cdk logp"() {

        Long notebookId = editable.notebookId
        Long editableId = editable.id
        Long cellId = 1

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "cdklogp")
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, CONFIG.basicCdkChemServicesBaseUrl+"/calculators/logp")

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
    }

    void "chemaxon logp"() {

        Long notebookId = editable.notebookId
        Long editableId = editable.id
        Long cellId = 1

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "cxnlogp")
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, CONFIG.basicChemaxonChemServicesBaseUrl+"/calculators/logp")


        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
    }

    void "chemaxon spherex"() {

        Long notebookId = editable.notebookId
        Long editableId = editable.id
        Long cellId = 1

        Map options = ['header.min_clusters':3, 'header.max_clusters':6]
        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "spherex")
                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, CONFIG.basicChemaxonChemServicesBaseUrl+"/descriptors/clustering/spherex/ecfp4")
                .withOption(StepDefinitionConstants.OPTION_SERVICE_PARAMS, options)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
    }
}
