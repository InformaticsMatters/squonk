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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Step that converts one data type to another. This is basically a wrapper around the Camel TypeConverter mechanism.
 * To create a ConverterStep use the static create() method. If a TypeConverter for the conversion is found in the Camel
 * TypeConverterRegistry then a ConverterStep will be returned that is capable of performing the conversion.
 * <p>
 * Created by timbo on 06/01/16.
 */
public class ConverterStep<P, Q, R, S> extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(ConverterStep.class.getName());

    private final TypeConverter converter;
    private final String variableName;
    private final VariableKey variableKey;

    private ConverterStep(TypeConverter converter,
                          String variableName,
                          VariableKey variableKey) {
        this.converter = converter;
        this.variableName = variableName;
        this.variableKey = variableKey;
    }

    public VariableKey getVariableKey() {
        return variableKey;
    }

    public String getVariableName() {
        return variableName;
    }

    /**
     * Create a ConverterStep for the specified conversion. If a TypeConverter cannot be found then null is returned.
     *
     * @param context         The CamelContext with the TypeConverterRegistry
     * @param cellId          The ID of the cell
     * @param jobId           The job ID
     * @param from            The source IODescriptor
     * @param to              The destination IODescriptor
     * @param inputVariableKey Where to get the input to convert
     * @param outputVariableName  The variable that that converted input will need to be sent to (typically "output")
     * @param generatedOutputName The variable name for the output
     * @return The constructed ConverterStep, or null if no conversion is possible
     */
    public static <P, Q, R, S> ConverterStep create(
            CamelContext context,
            Long cellId,
            String jobId,
            IODescriptor<P, Q> from,
            IODescriptor<R, S> to,
            VariableKey inputVariableKey,
            String outputVariableName,
            String generatedOutputName) {

        TypeConverter typeConverter = context.getTypeConverterRegistry().lookup(to.getPrimaryType(), from.getPrimaryType());
        if (typeConverter != null) {

            ConverterStep step = new ConverterStep(typeConverter, outputVariableName, new VariableKey(cellId, generatedOutputName));

            step.configure(
                    cellId,
                    jobId,
                    Collections.emptyMap(),
                    new IODescriptor[]{from},
                    new IODescriptor[]{to},
                    Collections.singletonMap("input", inputVariableKey),
                    Collections.singletonMap("output", generatedOutputName));
            return step;
        } else {
            return null;
        }
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = "Fetching input";
        IODescriptor<P,Q> from = getInputs()[0];
        IODescriptor<R,S> to = getOutputs()[0];
        P input = fetchMappedInput(StepDefinitionConstants.VARIABLE_INPUT_DATASET, from.getPrimaryType(), from.getSecondaryType(), varman, true);
        if (input == null) {
            throw new IllegalStateException("Input variable not found");
        }

        Map<String,Object> results = executeWithData(Collections.singletonMap(StepDefinitionConstants.VARIABLE_INPUT_DATASET, input), context);
        R result = (R)results.values().iterator().next();

        statusMessage = "Writing output";
        createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, to.getPrimaryType(), result, varman);
        LOG.info("Wrote input as output");
        statusMessage = "ConverterStep completed";
    }

    @Override
    public Map<String, Object> executeWithData(Map<String, Object> inputs, CamelContext context) throws Exception {
        statusMessage = "Converting inputs";
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Must only have one input");
        }
        IODescriptor<R,S> to = getOutputs()[0];
        R output = converter.convertTo(to.getPrimaryType(), inputs.values().iterator().next());
        return Collections.singletonMap(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, output);
    }


}
