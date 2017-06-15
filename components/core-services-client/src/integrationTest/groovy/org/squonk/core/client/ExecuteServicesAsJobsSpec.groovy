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

package org.squonk.core.client

import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.jobdef.JobStatus
import org.squonk.jobdef.StepsCellExecutorJobDefinition
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.notebook.api.VariableKey
import spock.lang.Stepwise

/**
 * Created by timbo on 01/06/16.
 */
@Stepwise
class ExecuteServicesAsJobsSpec extends ClientSpecBase {

    static def inputs = [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[]
    static def outputs = [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[]



    void setupSpec() {
        doSetupSpec("ExecuteServicesAsJobsSpec")
    }

    // get the services first as we need these to be loaded to execute
    void "list services"() {
        when:
        def configs = getServiceConfigs()
        println "found ${configs.size()} service configs"

        then:
        configs.size() > 20
    }

    void "get specified service"() {
        when:
        def config = getServiceConfigs()["cdk.logp"]

        then:
        config != null
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

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME, "cdk.logp")
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
                .withOutputVariableMapping("output", "cdklogp")

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "cdklogp") == 36
    }

    void "chemaxon logp"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME, "chemaxon.calculators.logp")
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
                .withOutputVariableMapping("output", "cxnlogp")


        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "cxnlogp") == 36
    }

    void "chemaxon spherex"() {

        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME, "chemaxon.clustering.sperex")
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
                .withOutputVariableMapping("output", "spherex")
                .withOption('header.min_clusters', 3)
                .withOption('header.max_clusters', 6)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "spherex") == 36
    }

//    void "chemaxon screen"() {
//
//        StepDefinition step = new StepDefinition(StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME)
//                .withInputs(inputs)
//                .withOutputs(outputs)
//                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, "input"))
//                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "screen")
//                .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, CONFIG.basicChemaxonChemServicesBaseUrl+"/descriptors/screening/ecfp4")
//                .withOption('option.filter', true)
//                .withOption('header.threshold', 0.5)
//                .withOption('header.structure_source', 'Oc1cccc(c1)c2nc(N3CCOCC3)c4oc5ncccc5c4n2')
//                .withOption('header.structure_format', 'smiles')
//
//        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(notebookId, editableId, cellId, step)
//        jobdef.configureCellAndSteps(notebookId, editableId, cellId, step)
//
//        when:
//        JobStatus status1 = jobClient.submit(jobdef, username, null)
//        JobStatus status2 = waitForJob(status1.jobId)
//
//        then:
//        status1.status == JobStatus.Status.RUNNING
//        status2.status == JobStatus.Status.COMPLETED
//        int count = findResultSize(notebookId, editableId, cellId, "screen")
//        count >= 1
//        count < 36
//    }
}
