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

package org.squonk.core.client

import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.impl.DatasetHttpExecutorStep
import org.squonk.io.IODescriptors
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.jobdef.JobStatus
import org.squonk.jobdef.StepsCellExecutorJobDefinition
import org.squonk.notebook.api.VariableKey
import org.squonk.reader.SDFReader
import org.squonk.types.SDFile
import spock.lang.Stepwise

/**
 * Created by timbo on 01/06/16.
 */
@Stepwise
class ExecuteServicesAsJobsSpec extends ClientSpecBase {

    static def inputs = IODescriptors.createMoleculeObjectDatasetArray("input")
    static def outputs = IODescriptors.createMoleculeObjectDatasetArray("output")



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
        def config = getServiceConfigs()["rdkit.calculators.verify"]

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
        int count = findResultSize(notebookId, editableId, 1, "output")

        then:
        count == datasetSize
    }

    void "cdk convert to sdf"() {

        // this tests a CDK bug that prevents SDF being written for large number of records
        // the bug was present in CDK version 2.1.1 and fixed in 2.2

        Dataset dataset = Molecules.datasetFromSDF(Molecules.BUILDING_BLOCKS_SDF) // has 7003 records
        def bigVar = new VariableKey(sourceCellId, "sdf")
        loadDataset(dataset, bigVar)

        def sdfOutput = IODescriptors.createSDFArray("output")
        StepDefinition step = new StepDefinition(DatasetHttpExecutorStep.class, "cdk.export.sdf")
                .withInputs(inputs)
                .withOutputs(sdfOutput)
                .withInputVariableMapping("input", bigVar)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, sdfOutput, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)
        InputStream is = readData(notebookId, editableId, cellId, "output")
        SDFile sdf = new SDFile(is, true)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        countSdf(sdf) == 7003
    }

    void "cdk logp"() {

        StepDefinition step = new StepDefinition(DatasetHttpExecutorStep.class, "cdk.logp")
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "output") == datasetSize
    }

    void "chemaxon logp"() {

        StepDefinition step = new StepDefinition(DatasetHttpExecutorStep.class, "chemaxon.calculators.logp")
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "output") == datasetSize
    }

    void "chemaxon spherex"() {

        StepDefinition step = new StepDefinition(DatasetHttpExecutorStep.class, "chemaxon.clustering.sperex")
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
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
        findResultSize(notebookId, editableId, cellId, "output") == datasetSize
    }

//    void "chemaxon screen"() {
//
//        StepDefinition step = new StepDefinition(StepDefinitionConstants.DatasetHttpExecutor.CLASSNAME)
//                .withInputs(inputs)
//                .withOutputs(outputs)
//                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, sourceVariableKey)
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

    int countSdf(input) {
        SDFReader reader = new SDFReader(input.getInputStream())
        int count = 0
        reader.each {
            count++
        }
        return count
    }
}
