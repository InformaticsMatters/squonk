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

import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.MoleculeObject;
import org.squonk.chembl.ChemblClient;
import org.squonk.dataset.Dataset;
import org.apache.camel.CamelContext;
import org.squonk.types.io.JsonHandler;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ChemblActivitiesFetcherStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(ChemblActivitiesFetcherStep.class.getName());

    static final String OPTION_ASSAY_ID = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_ASSAY_ID;
    static final String OPTION_PREFIX = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_PREFIX;
    static final String OPTION_BATCH_SIZE = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_BATCH_SIZE;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        Map<String, Object> results = executeForVariables(Collections.emptyMap(), context);
        Dataset dataset = (Dataset)results.get(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET);

        createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, Dataset.class, dataset, varman);
        statusMessage = generateStatusMessage(-1, dataset.getSize(), -1);
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(dataset.getMetadata()));
    }

    @Override
    public Map<String, Object> executeForVariables(Map<String, Object> inputs, CamelContext context) throws Exception {
        dumpConfig(Level.INFO);

        int batchSize = getOption(OPTION_BATCH_SIZE, Integer.class, 500);
        String prefix = getOption(OPTION_PREFIX, String.class);
        String assayID = getOption(OPTION_ASSAY_ID, String.class);
        if (assayID == null) {
            throw new IllegalStateException("Assay ID to fetch not specified");
        }

        ChemblClient client = new ChemblClient();
        statusMessage = "Fetching data ...";
        Dataset<MoleculeObject> results = client.fetchActivitiesForAssay(assayID, batchSize, prefix);
        return Collections.singletonMap(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, results);
    }
}
