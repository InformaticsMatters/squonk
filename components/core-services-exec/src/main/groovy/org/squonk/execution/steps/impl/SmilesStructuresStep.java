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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by timbo on 13/09/16.
 */
public class SmilesStructuresStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(SmilesStructuresStep.class.getName());

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.smiles.v1",
            "SmilesStructures",
            "Generate a dataset from provided SMILES strings",
            new String[]{"structure", "smiles", "dataset"},
            null, "icons/molecule.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            null,
            IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{

                    new OptionDescriptor<>(new MultiLineTextTypeDescriptor(10, 80, MultiLineTextTypeDescriptor.MIME_TYPE_TEXT_PLAIN),
                            StepDefinitionConstants.SmilesStructures.OPTION_SMILES, "Smiles",
                            "Smiles as text, with optional name", OptionDescriptor.Mode.User)

            },
            null, null, null,
            SmilesStructuresStep.class.getName()
    );

    protected static final String OPTION_SMILES = StepDefinitionConstants.SmilesStructures.OPTION_SMILES;
    protected static final String FIELD_NAME = "Name";


    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs, CamelContext context) throws Exception {
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
