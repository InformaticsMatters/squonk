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
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 13/09/16.
 */
public class SmilesStructuresStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(SmilesStructuresStep.class.getName());

    protected static final String OPTION_SMILES = StepDefinitionConstants.SmilesStructures.OPTION_SMILES;
    protected static final String FIELD_NAME = "Name";

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        Map<String,Object> results = executeForVariables(Collections.emptyMap(), context);
        Dataset result = getSingleDatasetFromMap(results);

        createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, Dataset.class, result, varman);
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(result.getMetadata()));
    }

    @Override
    public Map<String, Object> executeForVariables(Map<String, Object> inputs, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;


        String text = getOption(OPTION_SMILES, String.class);
        if (text == null) {
            throw new IllegalStateException("Smiles structures must be defined as option named " + OPTION_SMILES);
        }

        String[] lines = text.split("\n");
        List<MoleculeObject> mols = new ArrayList<>();
        boolean hasName = false;
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 0) {
                String[] parts = line.split("\\s+", 2);
                if (parts.length > 0) {
                    MoleculeObject mo = new MoleculeObject(parts[0], "smiles");
                    if (parts.length == 2) {
                        hasName = true;
                        mo.putValue(FIELD_NAME, parts[1]);
                    }
                    mols.add(mo);
                }
            }
        }
        DatasetMetadata<MoleculeObject> meta = new DatasetMetadata<>(MoleculeObject.class);
        meta.getProperties().put(DatasetMetadata.PROP_CREATED, DatasetMetadata.now());
        meta.getProperties().put(DatasetMetadata.PROP_SOURCE, "User provided Smiles");
        meta.getProperties().put(DatasetMetadata.PROP_DESCRIPTION, "Read from user provided Smiles");
        if (hasName) {
            meta.createField(FIELD_NAME, "User provided name", "Name provided by user with smiles", String.class);
        }
        meta.setSize(mols.size());
        statusMessage = mols.size() + " molecules";

        Dataset<MoleculeObject> result = new Dataset<>(mols, meta);
        return Collections.singletonMap(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, result);
    }


}
