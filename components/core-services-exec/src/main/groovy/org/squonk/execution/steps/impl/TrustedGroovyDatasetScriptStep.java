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
import org.squonk.execution.steps.AbstractStep;
import org.squonk.util.GroovyScriptExecutor;
import org.apache.camel.CamelContext;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import javax.script.ScriptEngine;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class TrustedGroovyDatasetScriptStep extends AbstractDatasetStep {

    private static final Logger LOG = Logger.getLogger(TrustedGroovyDatasetScriptStep.class.getName());

    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;


    protected Dataset doExecuteWithDataset(Dataset input, CamelContext camelContext) throws Exception {

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
        return results;
    }
}
