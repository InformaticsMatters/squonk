package org.squonk.core.client

import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.jobdef.JobStatus
import org.squonk.jobdef.StepsCellExecutorJobDefinition
import org.squonk.core.client.config.SquonkClientConfig
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.notebook.api.VariableKey
import spock.lang.Stepwise

/**
 * Created by timbo on 01/06/16.
 */
@Stepwise
class ExecuteServicesAsJobsSpec extends AbstractExecuteDocker {

    static SquonkClientConfig CONFIG = new SquonkClientConfig()

    void setupSpec() {
        doSetupSpec("ExecuteServicesAsJobsSpec")
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

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "cdklogp") == 36
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

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "cxnlogp") == 36
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

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "spherex") == 36
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

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        int count = findResultSize(notebookId, editableId, cellId, "screen")
        count >= 1
        count < 36
    }
}
