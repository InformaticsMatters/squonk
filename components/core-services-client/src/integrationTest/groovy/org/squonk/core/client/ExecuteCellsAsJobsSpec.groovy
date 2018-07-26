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

import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.impl.DatasetSelectSliceStep
import org.squonk.execution.steps.impl.NoopStep
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.jobdef.JobStatus
import org.squonk.jobdef.StepsCellExecutorJobDefinition
import org.squonk.notebook.api.VariableKey
import org.squonk.util.CommonMimeTypes
import spock.lang.IgnoreIf
import spock.lang.Stepwise

import java.util.zip.GZIPInputStream

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
        int count = findResultSize(notebookId, editableId, cellId, "input")

        then:
        count == 36
    }

    void "noop cell"() {

        StepDefinition step = new StepDefinition(NoopStep.class.getName())
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))

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

        StepDefinition step = new StepDefinition(DatasetSelectSliceStep.class.getName())
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
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

    void "docker cell no conversion"() {

        StepDefinition step = new StepDefinition(DockerProcessDataset.CLASSNAME)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
                .withOutputVariableMapping("output", "docker")
                .withOption(OPTION_DOCKER_IMAGE, "busybox")
                .withOption(OPTION_MEDIA_TYPE_INPUT, CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption(OPTION_MEDIA_TYPE_OUTPUT, CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption(DockerProcessDataset.OPTION_DOCKER_COMMAND,
                '''#!/bin/sh
cp input.meta output.meta
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
        findResultSize(notebookId, editableId, cellId, "docker") == 36
    }

//    /** Step that specifies it reads/writes a SDFile, but the input/output of the job is a Dataset so conversion is needed.
//     * StepExecutor should handle the conversions
//     */
//    void "docker cell sdf conversion"() {
//
//        StepDefinition step = new StepDefinition(DockerProcessDataset.CLASSNAME)
//                .withInputs(inputs)
//                .withOutputs(outputs)
//                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
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

    void "cdk sdf convert cell"() {

        StepDefinition step1 = new StepDefinition(DatasetServiceExecutor.CLASSNAME)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
                .withOutputVariableMapping("output", "sdf")
                .withOption("header.Content-Encoding", "gzip")
                .withOption("header.Accept-Encoding", "gzip")
                .withOption("header.Content-Type", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withOption("header.Accept", CommonMimeTypes.MIME_TYPE_MDL_SDF)
                .withOption(OPTION_SERVICE_ENDPOINT, "http://chemservices:8080/chem-services-cdk-basic/rest/v1/converters/dataset_to_sdf")


        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition()
        jobdef.configureCellAndSteps(notebookId, editableId, cellId, inputs, outputs, step1)

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

    @IgnoreIf({ MiniShift.RUNNING })
    void "groovy in docker cell noop"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScript.CLASSNAME)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
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

    @IgnoreIf({ MiniShift.RUNNING })
    void "groovy in docker cell using consumer"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScript.CLASSNAME)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
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

    @IgnoreIf({ MiniShift.RUNNING })
    void "groovy in docker cell using function"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScript.CLASSNAME)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
                .withOutputVariableMapping("output", "groovy-api-function")
                .withOption(UntrustedGroovyDatasetScript.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import org.squonk.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.stream.Stream
import java.util.function.Function

processDatasetStream('/source/input','/source/output') { Stream<MoleculeObject> stream ->
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

    @IgnoreIf({ MiniShift.RUNNING })
    void "groovy in docker cell using strong typing"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScript.CLASSNAME)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
                .withOutputVariableMapping("output", "groovy-api-strong")
                .withOption(UntrustedGroovyDatasetScript.OPTION_SCRIPT,
                '''
@GrabResolver(name='local', root='file:///var/maven_repo/')
@Grab(group='org.squonk.components', module='common', version='0.2-SNAPSHOT')
import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject
import static org.squonk.util.MoleculeObjectUtils.*
import java.util.stream.Stream

Dataset dataset = readDatasetFromFiles('/source/input')
Stream<MoleculeObject> stream = dataset.stream.peek() { MoleculeObject mo ->
    mo.putValue("hello", "world")
}
dataset.replaceStream(stream)
writeDatasetToFiles(dataset, '/source/output', true)
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


    @IgnoreIf({ MiniShift.RUNNING })
    void "groovy in docker cell using weak typing"() {

        StepDefinition step = new StepDefinition(UntrustedGroovyDatasetScript.CLASSNAME)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", new VariableKey(cellId, "input"))
                .withOutputVariableMapping("output", "groovy-api-weak")
                .withOption(UntrustedGroovyDatasetScript.OPTION_SCRIPT,
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
