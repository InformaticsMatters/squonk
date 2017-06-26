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
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.AbstractStandardStep;
import org.squonk.execution.variable.VariableManager;

import java.util.logging.Logger;

/** Simple step used for testing that reads a dataset as input and writes it to output
 *
 * Created by timbo on 06/01/16.
 */
public class EchoDatasetStep extends AbstractStandardStep {

    private static final Logger LOG = Logger.getLogger(EchoDatasetStep.class.getName());

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        Dataset input = fetchMappedInput("input", Dataset.class, varman, true);
        if (input == null) {
            throw new IllegalStateException("Input variable not found");
        }
        createMappedOutput("output", Dataset.class, input, varman);
        LOG.info("Wrote input as output");
    }
}
