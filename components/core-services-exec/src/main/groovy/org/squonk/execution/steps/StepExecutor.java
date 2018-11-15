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

package org.squonk.execution.steps;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
import org.squonk.jobdef.StepsCellExecutorJobDefinition;
import org.squonk.notebook.api.VariableKey;
import org.squonk.util.CamelRouteStatsRecorder;
import org.squonk.util.IOUtils;
import org.squonk.util.StatsRecorder;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Notebook executor for steps.
 *
 * One or more steps can be executed. Each step gets its input from the variable manager or the results of a previous
 * step. The source of the variable is defined by its 'producer ID' which is the ID of the cell that produced it.
 * This can be any cell in the notebook, including the cell itself (e.g. data produced by an earlier step).
 *
 * Data conversions are possible. If the available data is of a different type to that needed by the step the data
 * will be converted using the Camel TypeConverter mechanism. If no conversion is supported an exception will be thrown.
 * The JobDefinition defines the types of the inputs and outputs that will be written to the VariableManager.
 * Each step also defines the types of its inputs and outputs, allowing conversions to be performed at each stage.
 * For examples see the StepExecutorSpec unit test.
 *
 * The variable names must be unique during execution. A step MUST define input variable mappings that map between the
 * variable name and the name of the input for the step. A step can optionally define output variable mappings
 * which can be used to rename an output variable so that it is unique.
 *
 * Consider typical steps that have one input named 'input' and one output name 'output'.
 *
 * In a simple one step scenario the cell would define one one input named 'input' and one output name 'output'.
 * All that is needed is an input variable mapping that defines which cell provides the input and the name of that
 * cell's output that is to be used as this cell's input. That variable is fed to the step as the 'input' and its output
 * is written to the variable 'output' for the cell.
 *
 * In a more complex scenario you might have 2 cells, each of which expects one input named 'input' and one output named
 * 'output'. The output of cell 1 becomes the input of cell 2. This causes a problem as you can't have two variables
 * named 'input' (or two named 'output'). To address this you need to use output variable mappings for the first cell's
 * output to effectively rename the output to a unique name. For instance you map the name of the first cell's output from
 * 'output' to 'middle', and also provide an input variable mapping for the second cell that maps the cell's input named
 * 'input' to the variable named 'middle' (the mapping specifies that the producer's ID is the ID of the cell itself,
 * not another cell in the notebook).
 *
 * Other more complex scenarios are also possible, such as cells having multiple inputs and outputs, and steps after step
 * 1 using input from other cells in the notebook.
 *
 * If the job definition does not provide a definition for the cell's inputs the inputs of the first step are used.
 * If the job definition does not provide a definition for the cell's outputs the outputs of the last step are used.
 * This could be the case where e.g. there is only a single step and no conversions are needed.
 *
 *
 * @author timbo
 */
public class StepExecutor {

    private static final Logger LOG = Logger.getLogger(StepExecutor.class.getName());

    private final VariableManager varman;
    private final Long cellId;
    private final StepsCellExecutorJobDefinition jobdef;
    private final String jobId;
    private final String statsRoute;
    private volatile List<Step> steps = new ArrayList<>();
    private volatile Step currentStep;
    private final List<Map<String, Integer>> statsList = new ArrayList<>();

    public StepExecutor(Long cellId, String jobid, StepsCellExecutorJobDefinition jobdef, VariableManager varman) {
        this(cellId, jobid, jobdef, varman, null);
    }

    public StepExecutor(Long cellId, String jobid, StepsCellExecutorJobDefinition jobdef, VariableManager varman, String statsRoute) {
        this.cellId = cellId;
        this.jobdef = jobdef;
        this.jobId = jobid;
        this.varman = varman;
        this.statsRoute = statsRoute;
        LOG.info("Created StepExecutor for job " + jobId + " for cell " + cellId + " handling inputs "
                + IOUtils.joinArray(jobdef.getInputs(), ",") + " and outputs " + IOUtils.joinArray(jobdef.getOutputs(), ","));
    }

    public List<Map<String, Integer>> getExecutionsStats() {
        return statsList;
    }

    private <K,V> Map<K,V> fetchMapNotNull(Map<K,V> valuesOrNull) {
        return valuesOrNull == null ? Collections.emptyMap() : valuesOrNull;
    }

    private IODescriptor[] fetchIODescriptorsNotNull(IODescriptor[] valuesOrNull) {
        return valuesOrNull == null ? new IODescriptor[0] : valuesOrNull;
    }


    public void execute(CamelContext context) throws Exception {

        StepDefinition[] stepDefs = jobdef.getSteps();

        if (stepDefs == null || stepDefs.length == 0) {
            throw new IllegalStateException("No step definitions present");
        }

        final InputHolder inputs = new InputHolder();

        final IODescriptor[] jobInputDescriptors = fetchIODescriptorsNotNull(jobdef.getInputs());
        final IODescriptor[] jobOutputDescriptors = fetchIODescriptorsNotNull(jobdef.getOutputs());
        LOG.info("Job input descriptors: " + Arrays.stream(jobInputDescriptors)
                .map((iod) -> iod.getName())
                .collect(Collectors.joining(",")));
        LOG.info("Job output descriptors: " + Arrays.stream(jobOutputDescriptors)
                .map((iod) -> iod.getName())
                .collect(Collectors.joining(",")));

        StepDefinition currentStepDef = null;
        for (int i = 0; i < stepDefs.length; i++) {
            currentStepDef = stepDefs[i];

            // define the step
            Class cls = Class.forName(currentStepDef.getImplementationClass());
            Step step = (Step) cls.newInstance();
            currentStep = step;
            steps.add(step);
            ServiceDescriptor sd = currentStepDef.getServiceDescriptor();
            step.configure(jobId, currentStepDef.getOptions(), sd);

            // define details of the inputs
            final IODescriptor[] stepInputDescriptors = fetchIODescriptorsNotNull(currentStepDef.getInputs());
            final IODescriptor[] stepOutputDescriptors = fetchIODescriptorsNotNull(currentStepDef.getOutputs());
            final Map<String, VariableKey> stepInputVariableMappings = fetchMapNotNull(currentStepDef.getInputVariableMappings());
            final Map<String, String> stepOutputVariableMappings = fetchMapNotNull(currentStepDef.getOutputVariableMappings());

            LOG.info("Step input descriptors: " + Arrays.stream(stepInputDescriptors).map((iod) -> iod.getName()).collect(Collectors.joining(",")));
            LOG.info("Step output descriptors: " + Arrays.stream(stepOutputDescriptors).map((iod) -> iod.getName()).collect(Collectors.joining(",")));
            LOG.info("Step input mappings: " + stepInputVariableMappings.entrySet().stream()
                    .map((e) -> {
                        String name = e.getKey();
                        VariableKey value = e.getValue();
                        if (value == null) {
                            return name + " -> undefined";
                        } else {
                            return name + " -> " + value.getCellId() + ":" + value.getVariableName();
                        }
                    })
                    .collect(Collectors.joining(",")));
            LOG.info("Step output mappings: " + stepOutputVariableMappings.entrySet().stream()
                    .map((e) -> e.getKey() + "->" + e.getValue())
                    .collect(Collectors.joining(",")));

            Map<String, Object> stepInputs = new LinkedHashMap<>();


            for (IODescriptor iod : stepInputDescriptors) {
                String variableName = iod.getName();
                VariableKey key = stepInputVariableMappings.get(iod.getName());
                if (key == null) {
                    LOG.info("Variable binding for " + variableName + " not present. Ignoring variable");
                } else {
                    Object input;
                    // this IODescriptor is the one provided by the job that defines the actual data types of the inputs
                    IODescriptor d = findIODescriptor(jobInputDescriptors, variableName);
                    if (key.getCellId().equals(cellId)) {
                        // one of the inputs execution of the cell has already created e.g. output of a previous step
                        LOG.info(String.format("Using variable %s as type %s", iod.getName(), iod.getPrimaryType().getName()));
                        input = inputs.getInput(key.getCellId(), key.getVariableName());
                        if (input == null) {
                            // try as a saved variable
                            LOG.info(String.format("Reading cell's variable %s as type %s", iod.getName(), d.getPrimaryType().getName()));
                            input = varman.getValue(key, iod.getPrimaryType(), iod.getSecondaryType());
                            LOG.info("Read cell's variable: " + input);
                        }
                    } else if (d != null && d.getPrimaryType() != iod.getPrimaryType()) {
                        // the type the step needs is different to what is present so conversion is needed
                        // - should we also consider the secondary type when converting?
                        LOG.info(String.format("Reading variable %s as type %s", iod.getName(), d.getPrimaryType().getName()));
                        Object var = varman.getValue(key, d.getPrimaryType(), d.getSecondaryType());
                        LOG.info(String.format("Converting input variable %s from %s to %s", iod.getName(),
                                d.getPrimaryType().getName(), iod.getPrimaryType().getName()));
                        Object converted = context.getTypeConverter().convertTo(iod.getPrimaryType(), var);
                        if (converted == null) {
                            throw new IllegalStateException(String.format("Can't convert input %s to %s",
                                    d.getPrimaryType().getName(), iod.getPrimaryType().getName()));
                        }
                        input = converted;
                        inputs.setInput(key.getCellId(), iod.getName(), input);
                    } else {
                        // no type conversion needed
                        LOG.info(String.format("Reading variable %s as type %s", iod.getName(), iod.getPrimaryType().getName()));
                        input = varman.getValue(key, iod.getPrimaryType(), iod.getSecondaryType());
                        inputs.setInput(key.getCellId(), iod.getName(), input);
                    }
                    stepInputs.put(iod.getName(), input);
                }
            }

            // execute
            Map<String, Object> outputs = step.execute(stepInputs, context);

            // map the outputs to be the next inputs
            outputs.forEach((k, v) -> {
                String name = stepOutputVariableMappings.get(k);
                if (name == null) {
                    name = k;
                }
                inputs.setInput(cellId, name, v);
            });
            Map<String, Integer> stats = step.getUsageStats();
            statsList.add(stats);
        }

        // write the outputs
        for (IODescriptor iod : jobOutputDescriptors) {
            String name = iod.getName();
            Object value = inputs.getInput(cellId, name);
            if (value == null) {
                LOG.warning(String.format("Output %s not found", name));
            } else {
                if (iod.getPrimaryType().isAssignableFrom(value.getClass())) {
                    // no conversion needed
                    varman.putValue(new VariableKey(cellId, name), iod.getPrimaryType(), iod.getSecondaryType(), value);
                } else {
                    // data conversion needed
                    LOG.info(String.format("Converting output variable %s from %s to %s",
                            iod.getName(), value.getClass().getName(), iod.getPrimaryType().getName()));
                    Object converted = context.getTypeConverter().convertTo(iod.getPrimaryType(), value);
                    if (converted == null) {
                        throw new IllegalStateException(String.format("Can't convert output %s to %s",
                                iod.getPrimaryType().getName(), iod.getPrimaryType().getName()));
                    }
                    varman.putValue(new VariableKey(cellId, name), iod.getPrimaryType(), iod.getSecondaryType(), converted);
                }
            }
        }
        if (statsRoute != null && statsList.size() > 0) {
            // send stats
            ProducerTemplate pt = context.createProducerTemplate();
            pt.setDefaultEndpointUri(statsRoute);
            StatsRecorder recorder = new CamelRouteStatsRecorder(jobId, pt);
            recorder.recordStats(statsList);
        }
    }

    private IODescriptor findIODescriptor(IODescriptor[] iods, String name) {
        if (iods == null) {
            return null;
        }
        for (IODescriptor iod : iods) {
            if (name.equals(iod.getName())) {
                return iod;
            }
        }
        return null;
    }

    public String getCurrentStatus() {
        Step s = currentStep;
        if (s != null) {
            return s.getStatusMessage();
        }
        return null;
    }

    public List<Step> getExecutedSteps() {
        return steps;
    }


    /** Holds the variables that are fetched from other cells of produced during execution of this cell
     *
     */
    class InputHolder {

        Map<Long, Map<String, Object>> values = new LinkedHashMap<>();

        void setInput(Long producer, String name, Object value) {
            Map<String, Object> map = values.get(producer);
            if (map == null) {
                map = new LinkedHashMap<>();
                values.put(producer, map);
            }
            map.put(name, value);
        }

        Object getInput(Long producer, String name) {
            Map<String, Object> map = values.get(producer);
            return map == null ? null : map.get(name);
        }

    }
}
