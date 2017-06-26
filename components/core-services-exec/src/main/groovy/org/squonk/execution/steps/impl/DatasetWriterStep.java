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
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

/** Reads a dataset and writes it. The only real purpose of this is to take a temporary
 * dataset (PersistenceType.NONE) and make it persistent (PersistenceType.DATASET).
 * Generally you should not need to do this, but its available should you need to.
 *
 * @author timbo
 */
public class DatasetWriterStep extends AbstractStandardStep {

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        // the assumption is that the dataset has PersistenceType.NONE
        Dataset ds = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman);
        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, ds, varman);
    }

}
