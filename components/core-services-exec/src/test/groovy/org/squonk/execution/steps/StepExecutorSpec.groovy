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

package org.squonk.execution.steps

import org.apache.camel.Exchange
import org.apache.camel.TypeConversionException
import org.apache.camel.TypeConverter
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.TypeConverterSupport
import org.squonk.core.DockerServiceDescriptor
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.execution.steps.impl.EchoStringStep
import org.squonk.execution.steps.impl.IntegerToStringStep
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.notebook.api.VariableKey
import org.squonk.types.SDFile
import spock.lang.Ignore
import spock.lang.Specification

import java.util.zip.GZIPInputStream

/**
 * Created by timbo on 17/06/17.
 */
class StepExecutorSpec extends Specification {

    Long producer = 1
    Long notebook = 2
    Long editableId = 3
    Long cellId = 4

    void "simple 1 step"() {

        def varman = new VariableManager(null, notebook, 1);
        def inputVariableKey = new VariableKey(producer, "output")
        varman.putValue(inputVariableKey, String.class, "999")
        def inputs = [new IODescriptor("input", "text/plain", String.class)] as IODescriptor[]
        def outputs = [new IODescriptor("output", "text/plain", String.class)] as IODescriptor[]
        def stepDefs = [
                new StepDefinition(
                        EchoStringStep.class.getName(),
                        [:], // options
                        inputs,
                        outputs,
                        ["input": inputVariableKey], // inputVariableMappings
                        [:] //outputVariableMappings
                )
        ] as StepDefinition[]
        def jobdef = new ExecuteCellUsingStepsJobDefinition(
                notebook,
                editableId,
                cellId,
                inputs,
                outputs,
                stepDefs)

        def executor = new StepExecutor(cellId, "simple1step", jobdef, varman)

        def context = new DefaultCamelContext()
        context.start()

        when:
        executor.execute(stepDefs, context)
        def result = varman.getValue(new VariableKey(cellId, "output"), String.class)

        then:
        result == "999"
        executor.definedSteps.size() == 1
        executor.actualSteps.size() == 1

        cleanup:
        context?.shutdown()

    }

    void "implicit convert string to integer step"() {

        def varman = new VariableManager(null, notebook, 1);
        def inputVariableKey = new VariableKey(producer, "output")
        varman.putValue(inputVariableKey, String.class, "999")
        def inputs1 = [new IODescriptor("input", "text/plain", String.class)] as IODescriptor[]
        def outputs1 = [new IODescriptor("output", "text/plain", String.class)] as IODescriptor[]
        def inputs2 = [new IODescriptor("input", "number/integer", Integer.class)] as IODescriptor[]
        def outputs2 = [new IODescriptor("output", "text/plain", String.class)] as IODescriptor[]
        def stepDefs = [
                new StepDefinition(
                        EchoStringStep.class.getName(),
                        [:], // options
                        inputs1,
                        outputs1,
                        ["input": inputVariableKey], // inputVariableMappings
                        ["output": "_output0"] //outputVariableMappings
                ),
                // <------ converter step will be inserted here
                new StepDefinition(
                        IntegerToStringStep.class.getName(),
                        [:], // options
                        inputs2,
                        outputs2,
                        ["input": new VariableKey(cellId, "_output0")], // inputVariableMappings
                        ["output": "output"] //outputVariableMappings
                )
        ] as StepDefinition[]
        def jobdef = new ExecuteCellUsingStepsJobDefinition(
                notebook,
                editableId,
                cellId,
                inputs1,
                outputs2,
                stepDefs)

        def executor = new StepExecutor(cellId, "implicitConvertStep", jobdef, varman)

        def context = new DefaultCamelContext()
        context.start()

        when:
        executor.execute(stepDefs, context)
        def result = varman.getValue(new VariableKey(cellId, "output"), String.class)

        then:
        result == "999"
        executor.definedSteps.size() == 2
        executor.actualSteps.size() == 3 // one converter should have been added
        verifyMappings(executor.actualSteps[0], producer, "output", "_output0")
        verifyMappings(executor.actualSteps[1], cellId, "_output0", "_output0_0")
        verifyMappings(executor.actualSteps[2], cellId, "_output0_0", "output")

        cleanup:
        context?.shutdown()

    }

    private boolean verifyMappings(Step step, Long cellId, String inputName, String outputName) {
        step.inputVariableMappings["input"].cellId == cellId
        step.inputVariableMappings["input"].variableName == inputName
        step.outputVariableMappings["output"] == outputName
    }

    void "read from previous step convert string to integer step"() {

        Long cell1 = 1
        Long cell2 = 2

        def varman = new VariableManager(null, notebook, 1);
        def inputVariableKey = new VariableKey(cell1, "output")
        varman.putValue(inputVariableKey, String.class, "999")
        def inputs = [new IODescriptor("input", "number/integer", Integer.class)] as IODescriptor[]
        def outputs = [new IODescriptor("output", "text/plain", String.class)] as IODescriptor[]
        def stepDefs = [
                new StepDefinition(
                        IntegerToStringStep.class.getName(),
                        [:], // options
                        inputs,
                        outputs,
                        ["input": new VariableKey(cell1, "output")], // inputVariableMappings
                        [:] //outputVariableMappings
                )
        ] as StepDefinition[]
        def jobdef = new ExecuteCellUsingStepsJobDefinition(
                notebook,
                editableId,
                cellId,
                // the job specifies text but the step specifies integer so a conversion is needed
                [new IODescriptor("input", "text/plain", String.class)] as IODescriptor[],
                outputs,
                stepDefs)

        def executor = new StepExecutor(cell2, "implicitConvertStep", jobdef, varman)

        def context = new DefaultCamelContext()
        context.start()

        when:
        executor.execute(stepDefs, context)
        def converted = varman.getValue(new VariableKey(cell2, "_output_0"), Integer.class)
        def result = varman.getValue(new VariableKey(cell2, "output"), String.class)
        println "VARS: ${varman.tmpVariableInfo}"

        then:
        converted == 999
        result == "999"
        executor.definedSteps.size() == 1
        executor.actualSteps.size() == 2 // one converter should have been added

        cleanup:
        context?.shutdown()
    }

    @Ignore
    void "dataset read/write no convert step"() {

        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)

        def varman = new VariableManager(null, notebook, producer);
        def inputVariableKey = new VariableKey(producer, "input")
        varman.putValue(inputVariableKey, Dataset.class, dataset)

        def dsd = new DockerServiceDescriptor("dsd.id.1", "name", "description",
                new String[0], null,
                null, null, null,
                IODescriptors.createMoleculeObjectArray("input"),
                [new IORoute(IORoute.Route.FILE)] as IORoute[],
                IODescriptors.createMoleculeObjectArray("output"),
                [new IORoute(IORoute.Route.FILE)] as IORoute[],
                null,
                null,
                "org.squonk.execution.steps.impl.ThinDatasetDockerExecutorStep",
                "busybox",
                '#!/bin/sh\ncp input.metadata output.metadata\ncp input.data.gz output.data.gz',
                null)

        def stepDef = new StepDefinition(
                        dsd,
                        [:], // options
                        [(dsd.serviceConfig.inputDescriptors[0]): inputVariableKey], // inputVariableMappings
                        dsd.serviceConfig.outputDescriptors, //outputs
                )

        def jobdef = new ExecuteCellUsingStepsJobDefinition(
                notebook,
                editableId,
                cellId,
                stepDef)

        def executor = new StepExecutor(producer, "readwrotedatasetstep", jobdef, varman)

        def context = new DefaultCamelContext()
        context.start()

        when:
        executor.execute(jobdef.steps, context)
        def result = varman.getValue(new VariableKey(producer, "output"), Dataset.class)

        then:
        result.metadata.size == 36
        executor.definedSteps.size() == 1
        executor.actualSteps.size() == 1

        cleanup:
        context?.shutdown()
    }


    @Ignore
    void "dataset read/write step with convert to/from sdf"() {

        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)

        def varman = new VariableManager(null, notebook, producer);
        def inputVariableKey = new VariableKey(producer, "output")
        varman.putValue(inputVariableKey, Dataset.class, dataset)

        def dsd = new DockerServiceDescriptor("dsd.id.1", "name", "description",
                new String[0], null,
                null, null, null,
                IODescriptors.createMoleculeObjectArray("input"),
                [new IORoute(IORoute.Route.FILE, IODescriptors.createSDF("input"))] as IORoute[],
                IODescriptors.createMoleculeObjectArray("output"),
                [new IORoute(IORoute.Route.FILE, IODescriptors.createSDF("output"))] as IORoute[],
                null,
                null,
                "org.squonk.execution.steps.impl.ThinDatasetDockerExecutorStep",
                "busybox",
                '#!/bin/sh\ncp input.sdf.gz output.sdf.gz\n',
                null)

        def stepDef = new StepDefinition(
                dsd,
                [:], // options
                [(dsd.serviceConfig.inputDescriptors[0]): inputVariableKey], // inputVariableMappings
                dsd.serviceConfig.outputDescriptors, //outputs
        )

        def jobdef = new ExecuteCellUsingStepsJobDefinition(
                notebook,
                editableId,
                cellId,
                stepDef)

        def executor = new StepExecutor(cellId, "readwrotedatasetstep", jobdef, varman)

        def context = new DefaultCamelContext()
        // hack the type converter as we don't have the service running that will do this in Squonk
        context.getTypeConverterRegistry().addTypeConverter(SDFile.class, Dataset.class, new HackedDatasetToSdfConverter())


        context.start()

        when:
        executor.execute(jobdef.steps, context)
        def result = varman.getValue(new VariableKey(cellId, "output"), Dataset.class)

        then:
        result.metadata.size == 36
        executor.definedSteps.size() == 1
        executor.actualSteps.size() == 3

        cleanup:
        context?.shutdown()
    }

    class HackedDatasetToSdfConverter extends TypeConverterSupport {

        @Override
        def <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
            println "PERFORMING SDF CONVERSION"
            value.items
            return new SDFile(new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz"))
        }
    }
}
