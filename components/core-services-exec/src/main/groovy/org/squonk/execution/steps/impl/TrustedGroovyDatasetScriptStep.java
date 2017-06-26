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

import org.squonk.dataset.Dataset;
import org.squonk.util.GroovyScriptExecutor;
import org.apache.camel.CamelContext;
import org.squonk.execution.steps.AbstractStandardStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import javax.script.ScriptEngine;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class TrustedGroovyDatasetScriptStep extends AbstractStandardStep {

    private static final Logger LOG = Logger.getLogger(TrustedGroovyDatasetScriptStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;
        Dataset input = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman, true);
        //LOG.info("Input Dataset: " + input);
        String script = getOption(OPTION_SCRIPT, String.class);
        if (script == null) {
            throw new IllegalStateException("Script not defined. Should be present as option named " + OPTION_SCRIPT);
        }
        LOG.info("Script: " + script);

        Map bindings = Collections.singletonMap("input", input);

        ScriptEngine engine = GroovyScriptExecutor.createScriptEngine(this.getClass().getClassLoader());
        statusMessage = "Executing ...";
        Dataset results = GroovyScriptExecutor.executeAndReturnValue(Dataset.class, engine, script, bindings);
        LOG.info("Script executed");

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, results, varman);
        statusMessage = generateStatusMessage(input.getSize(), results.getSize(), -1);
        LOG.info("Results: " + results.getMetadata());
    }
}
