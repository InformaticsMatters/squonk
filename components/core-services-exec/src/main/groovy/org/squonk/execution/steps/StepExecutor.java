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

package org.squonk.execution.steps;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.impl.DefaultStepConverterStrategy;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
import org.squonk.jobdef.StepsCellExecutorJobDefinition;
import org.squonk.util.CamelRouteStatsRecorder;
import org.squonk.util.IOUtils;
import org.squonk.util.StatsRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author timbo
 */
public class StepExecutor {

    private static final Logger LOG = Logger.getLogger(StepExecutor.class.getName());

    private final VariableManager varman;
    private final Long producer;
    private final StepsCellExecutorJobDefinition jobdef;
    private final String jobId;
    private final String statsRoute;
    protected List<Step> definedSteps;
    protected List<Step> actualSteps;
    private volatile Step currentStep;

    public StepExecutor(Long producer, String jobid, StepsCellExecutorJobDefinition jobdef, VariableManager varman) {
        this(producer, jobid, jobdef, varman, null);
    }

    public StepExecutor(Long producer, String jobid, StepsCellExecutorJobDefinition jobdef, VariableManager varman, String statsRoute) {
        this.producer = producer;
        this.jobdef = jobdef;
        this.jobId = jobid;
        this.varman = varman;
        this.statsRoute = statsRoute;
        LOG.info("Created StepExecutor for job " + jobId + " for cell " + producer + " handling "
                + IOUtils.joinArray(jobdef.getInputs(), ",") + " -> " + IOUtils.joinArray(jobdef.getOutputs(), ","));
    }

    public void execute(CamelContext context) throws Exception {
        execute(jobdef.getSteps(), context);
    }

    @Deprecated
    public void execute(StepDefinition[] stepDefs, CamelContext context) throws Exception {


        definedSteps = new ArrayList<>();
        actualSteps = new ArrayList<>();

        if (stepDefs == null || stepDefs.length == 0) {
            throw new IllegalStateException("No step definitions present");
        }

        for (int i = 0; i < stepDefs.length; i++) {
            StepDefinition stepDef = stepDefs[i];
            Class cls = Class.forName(stepDef.getImplementationClass());
            Step step = (Step) cls.newInstance();

            ServiceDescriptor sd = stepDef.getServiceDescriptor();
            if (sd != null) {
                // we defer to the ServiceDescriptor for the IODescriptors
                LOG.info("====== Configuring step with Input IODescriptors: " + IOUtils.joinArray(sd.resolveInputIODescriptors(),","));
                LOG.info("====== Configuring step with Output IODescriptors: " + IOUtils.joinArray(sd.resolveOutputIODescriptors(),","));
                step.configure(
                        producer, jobId, stepDef.getOptions(),
                        stepDef.getInputVariableMappings(), stepDef.getOutputVariableMappings(),
                        sd);
            } else {
                // no ServiceDescriptor so we use what is defined by the step definition
                IODescriptor[] inputDescriptors = stepDef.getInputs();
                IODescriptor[] outputDescriptors = stepDef.getOutputs();
                step.configure(
                        producer, jobId, stepDef.getOptions(),
                        inputDescriptors, outputDescriptors,
                        stepDef.getInputVariableMappings(), stepDef.getOutputVariableMappings());
            }

            definedSteps.add(step);
        }

        StepConverterStrategy converterStrategy = new DefaultStepConverterStrategy(context);
        actualSteps = converterStrategy.addConverterSteps(producer, jobId, jobdef.getInputs(), definedSteps, jobdef.getOutputs());

        execute(actualSteps.toArray(new Step[actualSteps.size()]), context);
    }

    public void execute(Step[] steps, CamelContext context) throws Exception {

        StringBuilder b = new StringBuilder("Executing " + steps.length + " steps:\n");
        int i = 1;
        for (Step step : steps) {
            b.append("Step ").append(i).append(": ");
            b.append(step.toString()).append("\n");
            i++;
        }
        LOG.info(b.toString());

        List<Map<String, Integer>> statsList = new ArrayList<>();
        for (Step step : steps) {
            currentStep = step;
            step.execute(varman, context);
            Map<String, Integer> stats = step.getUsageStats();
            if (stats.size() > 0) {
                statsList.add(stats);
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

    public String getCurrentStatus() {
        Step s = currentStep;
        if (s != null) {
            return s.getStatusMessage();
        }
        return null;
    }

    public List<Step> getDefinedSteps() {
        return definedSteps;
    }

    public List<Step> getActualSteps() {
        return actualSteps;
    }
}
