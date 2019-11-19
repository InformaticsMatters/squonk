/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.types.MolFile;
import org.squonk.types.MoleculeObject;
import org.squonk.util.IOUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Reads a structure and generates a {@link MoleculeObject} of the corresponding format.
 * The structure is passed in as an {@link InputStream} (can be gzipped).
 *
 * @author timbo
 */
public class MolfileReaderStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(MolfileReaderStep.class.getName());

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.smiles.v1",
            "MolfileReader",
            "Generate a dataset from provided Molfile",
            new String[]{"structure", "molfile", "dataset"},
            null, "icons/molecule.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createStringArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            null, null, null, null,
            MolfileReaderStep.class.getName()
    );

    /**
     * Variable name for the MoleculeObjectDataset output
     */
    private static final String VAR_DATASET_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;


//    private String guessFileFormat(String filename) {
//        if (filename.toLowerCase().endsWith("mol") || filename.toLowerCase().endsWith("mol.gz")) {
//            return "mol";
//        } else if (filename.toLowerCase().endsWith("pdb") || filename.toLowerCase().endsWith("pdb.gz")) {
//            return "pdb";
//        } else {
//            throw new IllegalStateException("Cannot determine file format. Expected mol or pdb");
//        }
//    }

    @Override
    protected Map<String, Object> doExecute(Map<String, Object> inputs) throws Exception {
        statusMessage = "Reading structure";
        if (inputs == null) {
            throw new IllegalArgumentException("No input present");
        }
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Expected one input. Found " + inputs.size());
        }
        Object input = inputs.values().iterator().next();
        String mol;
        if (input instanceof String) {
            mol = (String) input;
        } else if (input instanceof InputStream) {
            mol = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream((InputStream) input));
        } else if (input instanceof MolFile) {
            InputStream is = ((MolFile)input).getGunzipedInputStream();
            mol = IOUtils.convertStreamToString(is);
        } else {
            throw new IllegalArgumentException("Unsupported input type: " + input.getClass().getName());
        }

        MoleculeObject mo = new MoleculeObject(mol, "mol");
        DatasetMetadata meta = new DatasetMetadata(MoleculeObject.class);
        meta.setSize(1);
        numProcessed = 1;
        numResults = 1;
        statusMessage = "Structure read";
        Dataset results = new Dataset(Collections.singletonList(mo), meta);
        return Collections.singletonMap(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, results);
    }
}