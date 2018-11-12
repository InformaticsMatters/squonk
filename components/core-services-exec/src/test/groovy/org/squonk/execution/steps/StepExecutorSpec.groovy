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

package org.squonk.execution.steps

import org.apache.camel.Exchange
import org.apache.camel.TypeConversionException
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.TypeConverterSupport
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.execution.steps.impl.DatasetSelectSliceStep
import org.squonk.execution.steps.impl.EchoStringStep
import org.squonk.execution.steps.impl.PlusStep
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.notebook.api.VariableKey
import org.squonk.types.MoleculeObject
import org.squonk.types.SDFile
import org.squonk.util.CommonMimeTypes
import org.squonk.util.IOUtils
import spock.lang.Specification

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
        executor.execute(context)
        def result = varman.getValue(new VariableKey(cellId, "output"), String.class)

        then:
        result == "999"
        executor.executionsStats.size() == 1

        cleanup:
        context?.shutdown()
    }

    void "simple 2 step"() {

        def varman = new VariableManager(null, notebook, 1);
        def inputVariableKey = new VariableKey(producer, "output")
        varman.putValue(inputVariableKey, Integer.class, 10)
        def inputs = [new IODescriptor("input", "text/plain", Integer.class)] as IODescriptor[]
        def outputs = [new IODescriptor("output", "text/plain", Integer.class)] as IODescriptor[]
        def stepDefs = [
                new StepDefinition(
                        PlusStep.class.getName(),
                        ["add":5], // options -->  10 + 5 = 15
                        inputs,
                        outputs,
                        ["input": inputVariableKey], // inputVariableMappings
                        ["output":"middle"] //outputVariableMappings
                ),
                new StepDefinition(
                        PlusStep.class.getName(),
                        ["add":20], // options -->  15 + 20 = 35
                        inputs,
                        outputs,
                        ["input": new VariableKey(cellId, "middle")], // inputVariableMappings
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
        executor.execute(context)
        def result = varman.getValue(new VariableKey(cellId, "output"), Integer.class)

        then:
        result == 35
        executor.executionsStats.size() == 2
        executor.executionsStats[0]["Plus"] == 1
        executor.executionsStats[1]["Plus"] == 1

        cleanup:
        context?.shutdown()
    }

    void "implicit convert string to integer step"() {

        def varman = new VariableManager(null, notebook, 1);
        def inputVariableKey = new VariableKey(producer, "output")
        varman.putValue(inputVariableKey, String.class, "90")
        def stepInputs = [new IODescriptor("input", "number/integer", Integer.class)] as IODescriptor[]
        def stepOutputs = [new IODescriptor("output", "number/integer", Integer.class)] as IODescriptor[]
        def jobInputs = [new IODescriptor("input", "text/plain", String.class)] as IODescriptor[]
        def jobOutputs = [new IODescriptor("output", "text/plain", String.class)] as IODescriptor[]
        def stepDefs = [
                new StepDefinition(
                        PlusStep.class.getName(),
                        ["add":5], // options
                        stepInputs,
                        stepOutputs,
                        ["input": inputVariableKey], // inputVariableMappings
                        [:] //outputVariableMappings
                )
        ] as StepDefinition[]
        def jobdef = new ExecuteCellUsingStepsJobDefinition(
                notebook,
                editableId,
                cellId,
                jobInputs,
                jobOutputs,
                stepDefs)

        def executor = new StepExecutor(cellId, "implicitConvertStep", jobdef, varman)

        def context = new DefaultCamelContext()
        context.start()

        when:
        executor.execute(context)
        def result = varman.getValue(new VariableKey(cellId, "output"), String.class)

        then:
        result == "95"
        result.getClass() == String.class
        executor.executionsStats.size() == 1
        executor.getCurrentStatus() == "90 + 5 = 95"

        cleanup:
        context?.shutdown()
    }


    void "dataset read/write no convert step"() {

        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)

        def varman = new VariableManager(null, notebook, cellId);
        def inputVariableKey = new VariableKey(producer, "output")
        varman.putValue(inputVariableKey, Dataset.class, dataset)
        def inputs = [new IODescriptor("input", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class)] as IODescriptor[]
        def outputs = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class)] as IODescriptor[]

        def stepDefs = [
                new StepDefinition(
                        DatasetSelectSliceStep.class.getName(),
                        [count:5], // options
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

        def executor = new StepExecutor(cellId, "readwritedatasetstep", jobdef, varman)

        def context = new DefaultCamelContext()
        context.start()

        when:
        executor.execute(context)
        def result = varman.getValue(new VariableKey(cellId, "output"), Dataset.class)

        then:
        result.metadata.size == 5
        executor.executionsStats.size() == 1

        cleanup:
        context?.shutdown()
    }

    /** Job defines SDF as input and output but the step needs a Dataset so conversion both ways is needed
     *
     */
    void "dataset read/write step with convert to/from sdf"() {

        SDFile sdf = new SDFile(new File(Molecules.KINASE_INHIBS_SDF), true)

        def varman = new VariableManager(null, notebook, cellId);
        def inputVariableKey = new VariableKey(producer, "output")
        varman.putValue(inputVariableKey, SDFile.class, sdf)
        def jobInputs = [new IODescriptor("input", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]
        def jobOutputs = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]
        def stepInputs = [new IODescriptor("input", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class)] as IODescriptor[]
        def stepOutputs = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class)] as IODescriptor[]

        def stepDefs = [
                new StepDefinition(
                        DatasetSelectSliceStep.class.getName(),
                        [count:5], // options
                        stepInputs,
                        stepOutputs,
                        ["input": inputVariableKey], // inputVariableMappings
                        [:] //outputVariableMappings
                )
        ] as StepDefinition[]
        def jobdef = new ExecuteCellUsingStepsJobDefinition(
                notebook,
                editableId,
                cellId,
                jobInputs,
                jobOutputs,
                stepDefs)

        def executor = new StepExecutor(cellId, "readwritedatasetconvertstep", jobdef, varman)

        def context = new DefaultCamelContext()
        // Hack the type converter as we don't have the service running that will do this in Squonk
        // There is already a converted for SDF -> Dataset that does not need any services
        context.getTypeConverterRegistry().addTypeConverter(SDFile.class, Dataset.class, new HackedDatasetToSdfConverter())

        context.start()

        when:
        executor.execute(context)
        def result = varman.getValue(new VariableKey(cellId, "output"), SDFile.class)
        def mockTest = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(result.inputStream))


        then:
        mockTest == "Mock SDF with 5 records"
        executor.executionsStats.size() == 1

        cleanup:
        context?.shutdown()
    }

    class HackedDatasetToSdfConverter extends TypeConverterSupport {

        @Override
        <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
            println "PERFORMING SDF CONVERSION"
            int size = value.items.size()
            return new SDFile("Mock SDF with " + size + " records", false)
        }
    }
}
