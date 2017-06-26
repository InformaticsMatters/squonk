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
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.Step;
import org.squonk.execution.steps.StepConverterStrategy;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;
import org.squonk.util.IOUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 17/06/17.
 */
public class DefaultStepConverterStrategy implements StepConverterStrategy {

    private static final Logger LOG = Logger.getLogger(DefaultStepConverterStrategy.class.getName());

    private final CamelContext context;

    public DefaultStepConverterStrategy(CamelContext context) {
        this.context = context;
    }

    private void validateParameters(IODescriptor[] from, IODescriptor[] to) {
        if (from.length != to.length) {
            throw new IllegalArgumentException("Input and output descriptors have different lengths: " + from.length + "," + to.length);
        }
    }


    /**
     * Create converter steps that are needed to convert data into a form needed by a step.
     *
     * @param cellId
     * @param jobId
     * @param from   The available types
     * @param toStep The step that needs the data
     * @return
     */
    protected List<ConverterStep> createConverterSteps(
            Long cellId, String jobId,
            IODescriptor[] from, Step toStep
    ) {

        IODescriptor[] to = toStep.getInputs();
        validateParameters(from, to);
        Map<String, VariableKey> inputMappings = toStep.getInputVariableMappings();
        List<ConverterStep> results = new ArrayList<>();

        for (int i = 0; i < from.length; i++) {
            if (needsConversion(from[i], to[i])) {

                IODescriptor toIod = to[i];
                String toName = toIod.getName();
                VariableKey inputKey = inputMappings.get(toName);
                if (inputKey == null) {
                    throw new IllegalStateException("No input mapping for variable " + toName + ". Values present are: "
                            + inputMappings.keySet().stream().collect(Collectors.joining(",")));
                }

                String generatedToName = inputKey.getVariableName() + "_" + i;
                if (!generatedToName.startsWith("_")) {
                    generatedToName = "_" + generatedToName;
                }

                ConverterStep converter = ConverterStep.create(context, cellId, jobId, from[i], to[i],
                        inputKey, toName, generatedToName);

                if (converter == null) {
                    throw new IllegalStateException("No converter for " + from[i] + " -> " + to[i]);
                }
                LOG.info("============ Adding converter step for " + from[i] + " -> " + to[i] +
                        " with generated output mapping of " + toName + " -> " + generatedToName);
                results.add(converter);
            }
        }
        return results;
    }

    /**
     * Create converters that are needed to convert the outputs of a final step to the required formats
     *
     * @param cellId
     * @param jobId
     * @param fromStep
     * @param to
     * @return
     */
    protected List<ConverterStep> createConverterSteps(
            Long cellId, String jobId,
            Step fromStep, IODescriptor[] to
    ) {

        IODescriptor[] from = fromStep.getOutputs();
        validateParameters(from, to);
        List<ConverterStep> results = new ArrayList<>();

        for (int i = 0; i < from.length; i++) {
            if (needsConversion(from[i], to[i])) {

                IODescriptor fromIod = from[i];
                IODescriptor toIod = to[i];
                String fromName = fromIod.getName();
                String toName = toIod.getName();
                String inputName = fromStep.getOutputVariableMappings().get(fromName);
                if (inputName == null) {
                    // output mapping is optional - if not defined the use the internal name
                    inputName = fromName;
                }
                VariableKey key = new VariableKey(cellId, inputName);

                ConverterStep converter = ConverterStep.create(context, cellId, jobId, from[i], to[i],
                        key, toName, null);

                if (converter == null) {
                    throw new IllegalStateException("No converter for " + from[i] + " -> " + to[i]);
                }
                LOG.info("============ Adding converter step for " + from[i] + " -> " + to[i] +
                        " with output mapped to " + toName);
                results.add(converter);
            }
        }
        return results;
    }

    /**
     * Intersperse any converter steps that are needed to execute this set of steps
     *
     * @param cellId
     * @param jobId  The job ID
     * @param from   The source IODescriptors
     * @param steps  The steps that might need converters
     * @param to     The destination IODescriptors
     * @return
     */
    @Override
    public List<Step> addConverterSteps(
            Long cellId, String jobId,
            IODescriptor[] from, List<Step> steps, IODescriptor[] to) {

        List<Step> results = new ArrayList<>();
        IODescriptor[] fromDescriptors = from;
        IODescriptor[] toDescriptors = null;

        Map<String, VariableKey> inputs = null;
        Step currentStep = null;

        int i = 0;

        for (Step step : steps) {
            currentStep = step;
            toDescriptors = step.getInputs();

            if (from != null) { // first step might not have any inputs
                List<ConverterStep> conversions = createConverterSteps(
                        cellId, jobId, fromDescriptors, step);

                if (!conversions.isEmpty()) {
                    results.addAll(conversions);
                    LOG.info("============ Added " + conversions.size() + " converter steps");
                    // Now we need to re-bind the mappings for the remaining steps to accommodate bindings of converter steps
                    if (i < steps.size()) {
                        for (int k = i; k < steps.size(); k++) {
                            Step followingStep = steps.get(k);
                            remapInputs(followingStep, conversions);
                        }
                    }
                }
            }
            // now that we've added the conversions we add the step itself
            results.add(step);

            // now the step's output must become the next step's input
            fromDescriptors = step.getOutputs();


            i++;
        }

        // finally add converters for the job's required outputs
        LOG.info("Final step available output: "
                + IOUtils.joinArray(fromDescriptors, ",")
                + " required output: " + IOUtils.joinArray(toDescriptors, ",")
        );
        // The FROM part is the output of the last step
        // the TO part is defined by the job's output
        toDescriptors = to;

        List<ConverterStep> finalConversions = createConverterSteps(
                cellId, jobId,
                currentStep, toDescriptors);

        if (!finalConversions.isEmpty()) {
            LOG.info("============ Added " + finalConversions.size() + " final converter steps");
            results.addAll(finalConversions);
        }

        return results;
    }

    private void remapInputs(Step followingStep, List<ConverterStep> conversions) {
        Map<String, VariableKey> inputMappings = followingStep.getInputVariableMappings();
        for (ConverterStep converter : conversions) {
            String name = converter.getVariableName();
            if (inputMappings.containsKey(name)) {
                VariableKey oldKey = inputMappings.put(converter.getVariableName(), converter.getVariableKey());
                LOG.info("Remapped input " + name + " of step " + followingStep.getClass().getSimpleName() + ": "
                        + oldKey.getVariableName() + " -> " + converter.getVariableKey());
            }
        }
    }

    private boolean needsConversion(IODescriptor from, IODescriptor to) {

        Class fromType = from.getPrimaryType();
        Class toType = to.getPrimaryType();
        if (fromType != toType) {
            // handle case where fromType is subclass of toType
            if (toType.isAssignableFrom(fromType)) {
                return false;
            }
            // TODO handle generic type
            return true;
        }
        return false;
    }

}
