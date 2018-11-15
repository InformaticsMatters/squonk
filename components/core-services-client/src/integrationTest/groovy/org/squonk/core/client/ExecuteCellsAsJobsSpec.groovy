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

import org.squonk.core.DefaultServiceDescriptor
import org.squonk.core.DockerServiceDescriptor
import org.squonk.dataset.Dataset
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.execution.steps.impl.DatasetMergerStep
import org.squonk.execution.steps.impl.DatasetSelectSliceStep
import org.squonk.execution.steps.impl.DefaultDockerExecutorStep
import org.squonk.execution.steps.impl.NoopStep
import org.squonk.execution.steps.impl.UntrustedGroovyDatasetScriptStep
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.jobdef.JobStatus
import org.squonk.jobdef.StepsCellExecutorJobDefinition
import org.squonk.notebook.api.VariableKey
import org.squonk.types.BasicObject
import org.squonk.util.CommonMimeTypes
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Stepwise

import static org.squonk.execution.steps.StepDefinitionConstants.*

public class MiniShift {

    public static final boolean RUNNING = running();

    static boolean running() {
        def proc = 'minishift status'.execute()
        try {
            proc.waitForOrKill(2000)
            def matcher = (proc.text =~ /(?m)Running/)
            return matcher.count > 0
        } catch (IOException) {
            // Don't care
        }
        return false;
    }

}

/**
 * Created by timbo on 01/06/16.
 */
@Stepwise
class ExecuteCellsAsJobsSpec extends ClientSpecBase {

    void setupSpec() {
        doSetupSpec("ExecuteCellsAsJobsSpec")
    }

    static def inputs = [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[]
    static def outputs = [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[]

    // get the services first as we need these to be loaded to execute
    void "list services"() {
        when:
        def configs = getServiceConfigs()
        println "found ${configs.size()} service configs"

        then:
        configs.size() > 20
    }


    void "list jobs"() {
        when:
        def jobs = jobClient.list(null)

        then:
        jobs != null
    }

    void "input loaded"() {
        when:
        int count = findResultSize(notebookId, editableId, sourceVariableKey.cellId, sourceVariableKey.variableName)

        then:
        count == 36
    }

    void "noop cell"() {

        StepDefinition step = new StepDefinition(NoopStep.class)
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
        findResultSize(notebookId, editableId, cellId, "output") == 36
    }

    void "dataset slice cell"() {

        StepDefinition step = new StepDefinition(DatasetSelectSliceStep.class)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
                .withOption(DatasetSelectSliceStep.OPTION_COUNT, 10)
                .withServiceDescriptor(DatasetSelectSliceStep.SERVICE_DESCRIPTOR)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "output") == 10
    }

    void "dataset merger cell"() {

        VariableKey sourceVariableKey1 = new VariableKey(sourceCellId, "output1")
        VariableKey sourceVariableKey2 = new VariableKey(sourceCellId, "output2")
        Dataset ds1 = new Dataset(BasicObject.class, [
                new BasicObject([id:1, a:"1", hello:'world']),
                new BasicObject([id:2,a:"99",hello:'mars',foo:'bar']),
                new BasicObject([id:3,a:"100",hello:'mum'])
        ])
        Dataset ds2 = new Dataset(BasicObject.class, [
                new BasicObject([id:2,b:"1",hello:'jupiter']),
                new BasicObject([id:3,b:"99",hello:'saturn',foo:'baz']),
                new BasicObject([id:4,b:"100",hello:'uranus'])
        ])

        loadDataset(ds1, sourceVariableKey1)
        loadDataset(ds2, sourceVariableKey2)

        StepDefinition step = new StepDefinition(DatasetMergerStep.class)
                .withInputs(DatasetMergerStep.SERVICE_DESCRIPTOR.serviceConfig.inputDescriptors)
                .withOutputs(DatasetMergerStep.SERVICE_DESCRIPTOR.serviceConfig.outputDescriptors)
                .withInputVariableMapping("input2", sourceVariableKey1)
                .withInputVariableMapping("input4", sourceVariableKey2)
                .withOption(DatasetMergerStep.OPTION_MERGE_FIELD_NAME, 'id')
                .withServiceDescriptor(DatasetMergerStep.SERVICE_DESCRIPTOR)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        def dataset = readDataset(notebookId, editableId, cellId, "output")
        dataset.generateMetadata()
        dataset.size == 4

        BasicObject bo2 = dataset.items.find {
            it.getValue('id') == 2
        }
        // original value should be retained
        bo2.getValue('hello') == 'mars'
    }

    void "dataset cdk logp cell"() {

        DefaultServiceDescriptor sd = new DefaultServiceDescriptor("dataset.cdk.logp", "logp", "logp",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, null, null, null,
                StepDefinitionConstants.DatasetHttpExecutor.CLASSNAME)


        StepDefinition step = new StepDefinition(DatasetSelectSliceStep.class)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
                .withServiceDescriptor(DatasetSelectSliceStep.SERVICE_DESCRIPTOR)

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        def dataset = readDataset(notebookId, editableId, cellId, "output")
        dataset.generateMetadata()
        dataset.size == datasetSize
        dataset.items[0].values.size() == 2
    }


    void "docker cell no conversion"() {

        DockerServiceDescriptor dsd = new DockerServiceDescriptor("docker.cell.no.conversion", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[], [new IORoute(IORoute.Route.FILE)] as IORoute[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[], [new IORoute(IORoute.Route.FILE)] as IORoute[],
                null, null, "executor", 'busybox', "", [:])

        StepDefinition step = new StepDefinition(DefaultDockerExecutorStep.class)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
                .withServiceDescriptor(dsd)
                .withOption(OPTION_DOCKER_IMAGE, "busybox")
                .withOption(OPTION_MEDIA_TYPE_INPUT, CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption(OPTION_MEDIA_TYPE_OUTPUT, CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption(DockerProcessDataset.OPTION_DOCKER_COMMAND,
                '''#!/bin/sh
cp input.metadata output.metadata
cp input.data.gz output.data.gz
''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "output") == 36
    }

//    /** Step that specifies it reads/writes a SDFile, but the input/output of the job is a Dataset so conversion is needed.
//     * StepExecutor should handle the conversions
//     */
//    void "docker cell sdf conversion"() {
//
//        StepDefinition step = new StepDefinition(DockerProcessDataset.CLASSNAME)
//                .withInputs(inputs)
//                .withOutputs(outputs)
//                .withInputVariableMapping("input", sourceVariableKey)
//                .withOutputVariableMapping("output", "docker")
//                .withOption(OPTION_DOCKER_IMAGE, "busybox")
//                .withOption(OPTION_MEDIA_TYPE_INPUT, CommonMimeTypes.MIME_TYPE_MDL_SDF)
//                .withOption(OPTION_MEDIA_TYPE_OUTPUT, CommonMimeTypes.MIME_TYPE_MDL_SDF)
//                .withOption(DockerProcessDataset.OPTION_DOCKER_COMMAND, '#!/bin/sh\ncp input.sdf.gz output.sdf.gz')
//
//        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
//        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)
//
//        when:
//        JobStatus status1 = jobClient.submit(jobdef, username, null)
//        JobStatus status2 = waitForJob(status1.jobId)
//
//        then:
//        status1.status == JobStatus.Status.RUNNING
//        status2.status == JobStatus.Status.COMPLETED
//        findResultSize(notebookId, editableId, cellId, "docker") == 36
//    }

    //@IgnoreIf({ MiniShift.RUNNING })
    @Ignore
    void "groovy in docker cell noop"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScriptStep.class)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
                .withOutputVariableMapping("output", "groovy-noop")
                .withOption(UntrustedGroovyDatasetScript.OPTION_SCRIPT,
                '''
def file1 = new File('input.meta')
file1.renameTo 'output.meta'
def file2 = new File('input.data.gz')
file2.renameTo 'output.data.gz'
''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId, 120)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-noop") == 36
    }

    //@IgnoreIf({ MiniShift.RUNNING })
    @Ignore
    void "groovy in docker cell using consumer"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScriptStep.class)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
                .withOutputVariableMapping("output", "groovy-api-consumer")
                .withOption(UntrustedGroovyDatasetScript.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import org.squonk.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.function.Consumer

processDataset('input','output') { MoleculeObject mo ->
    mo.putValue("hello", "world")
} as Consumer
''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)
        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId, 120)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-api-consumer") == 36
    }

    //@IgnoreIf({ MiniShift.RUNNING })
    @Ignore
    void "groovy in docker cell using function"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScriptStep.class)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
                .withOutputVariableMapping("output", "groovy-api-function")
                .withOption(UntrustedGroovyDatasetScript.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import org.squonk.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.stream.Stream
import java.util.function.Function

processDatasetStream('input','output') { Stream<MoleculeObject> stream ->
    return stream.peek() { MoleculeObject mo ->
        mo.putValue("hello", "world")
    }
} as Function
''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId, 120)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-api-function") == 36
    }

    //@IgnoreIf({ MiniShift.RUNNING })
    @Ignore
    void "groovy in docker cell using strong typing"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScriptStep.class)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
                .withOutputVariableMapping("output", "groovy-api-strong")
                .withOption(UntrustedGroovyDatasetScript.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.stream.Stream

Dataset dataset = readDatasetFromFiles('input')
Stream<MoleculeObject> stream = dataset.stream.peek() { MoleculeObject mo ->
    mo.putValue("hello", "world")
}
dataset.replaceStream(stream)
writeDatasetToFiles(dataset, 'output', true)
''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId, 120)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-api-strong") == 36
    }


    //@IgnoreIf({ MiniShift.RUNNING })
    @Ignore
    void "groovy in docker cell using weak typing"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScript)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", sourceVariableKey)
                .withOutputVariableMapping("output", "groovy-api-weak")
                .withOption(UntrustedGroovyDatasetScript.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import static org.squonk.util.MoleculeObjectUtils.*

def dataset = readDatasetFromFiles('input')
def stream = dataset.stream.peek() { mo ->
    mo.putValue("hello", "world")
}
dataset.replaceStream(stream)
writeDatasetToFiles(dataset, 'output', true)
''')

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step)

        when:
        JobStatus status1 = jobClient.submit(jobdef, username, null)
        JobStatus status2 = waitForJob(status1.jobId, 120)

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        findResultSize(notebookId, editableId, cellId, "groovy-api-weak") == 36
    }

}
