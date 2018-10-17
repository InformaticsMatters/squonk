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

import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.variable.VariableManager;

import java.util.Map;
import java.util.logging.Logger;

/** Simple step used for testing that reads text input and writes it to output
 *
 * Created by timbo on 06/01/16.
 */
public class EchoStringStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(EchoStringStep.class.getName());

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        String input = fetchMappedInput("input", String.class, varman);
        if (input == null) {
            throw new IllegalStateException("Input variable not found");
        }
        LOG.info("Input: " + input);

        createMappedOutput("output", String.class, input, varman);
        LOG.info("Wrote input as output");
    }

    @Override
    public Map<String, Object> executeWithData(Map<String, Object> inputs, CamelContext context) throws Exception {
        return inputs;
    }
}
