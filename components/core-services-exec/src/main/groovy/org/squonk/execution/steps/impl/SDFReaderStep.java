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
import org.squonk.io.InputStreamDataSource;
import org.squonk.io.SquonkDataSource;
import org.squonk.reader.CSVReader;
import org.squonk.reader.SDFReader;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Reads a SDFile and generates a {@link Dataset} of
 * {@link MoleculeObject}s. The SDFile is passed as an
 * {@link java.io.InputStream} (can be gzipped). By default the
 * input is expected in the variable named by the VAR_SDF_INPUT
 * constant, though that name can be mapped to a different name. The resulting
 * Dataset is contained in the variable named by the VAR_DATASET_OUTPUT constant.
 *
 * @author timbo
 */
public class SDFReaderStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(SDFReaderStep.class.getName());

    /**
     * How to handle the name field (the first line of the CTAB block). See
     * {@link SDFReader} for details.
     */
    public static final String OPTION_NAME_FIELD_NAME = StepDefinitionConstants.SdfUpload.OPTION_NAME_FIELD_NAME;
    /**
     * Expected variable name for the input
     */
    public static final String VAR_SDF_INPUT = StepDefinitionConstants.VARIABLE_FILE_INPUT;
    /**
     * Variable name for the MoleculeObjectDataset output
     */
    public static final String VAR_DATASET_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        LOG.info("execute SDFReaderStep");
        SquonkDataSource dataSource = fetchMappedInput(VAR_SDF_INPUT, SquonkDataSource.class, varman);

        Map<String, Object> results = executeWithData(Collections.singletonMap("input", dataSource), context);
        Dataset result = (Dataset)results.values().iterator().next();

        LOG.fine("Writing output");
        createMappedOutput(VAR_DATASET_OUTPUT, Dataset.class, result, varman);
        statusMessage = generateStatusMessage(-1, result.getSize(), -1);
        LOG.fine("Writing dataset from SDF complete: " + JsonHandler.getInstance().objectToJson(result.getMetadata()));
    }

    private SDFReader createReader(SquonkDataSource dataSource) throws IOException {
        SDFReader reader = new SDFReader(dataSource);
        String nameFieldName = getOption(OPTION_NAME_FIELD_NAME, String.class);
        if (nameFieldName != null && nameFieldName.length() > 0) {
            reader.setNameFieldName(nameFieldName);
        } else {
            reader.setNameFieldName(null);
        }
        return reader;
    }

    @Override
    public Map<String, Object> executeWithData(Map<String, Object> inputs, CamelContext context) throws Exception {
        statusMessage = "Reading SDF ...";
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Must provide a single input");
        }
        Object input = inputs.values().iterator().next();
        SquonkDataSource dataSource;
        if (input instanceof SquonkDataSource) {
            dataSource = (SquonkDataSource)input;
        } else if (input instanceof InputStream) {
            dataSource = new InputStreamDataSource("input", "", (InputStream)input, null);
        } else {
            throw new IllegalArgumentException("Unsupported input type: " + input.getClass().getName());
        }
        dataSource.setGzipContent(false);
        SDFReader reader = createReader(dataSource);
        Stream<MoleculeObject> mols = reader.asStream().onClose(() -> {
            try {
                dataSource.getInputStream().close();
            } catch (IOException ioe) {
                LOG.warning("Failed to close InputStream");
            }
        });
        Dataset results = new Dataset(mols, reader.getDatasetMetadata());
        return Collections.singletonMap("output", results);
    }

}
