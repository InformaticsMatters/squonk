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
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.io.InputStreamDataSource;
import org.squonk.io.SquonkDataSource;
import org.squonk.options.FileTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.reader.SDFReader;
import org.squonk.types.MoleculeObject;
import org.squonk.types.SDFile;
import org.squonk.util.CommonMimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
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

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.import.sdf.v1",
            "SdfUpload",
            "SDF upload",
            new String[]{"file", "upload", "sdf"},
            null, "icons/file_upload_molecule.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            null,
            new IODescriptor[]{
                    IODescriptors.createCSV("fileContent"),
                    IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET)
            },
            new OptionDescriptor[]{

                    new OptionDescriptor<>(new FileTypeDescriptor(new String[]{"sdf"}),
                            StepDefinitionConstants.SdfUpload.OPTION_FILE_UPLOAD,
                            "SD File",
                            "Upload SD file",
                            OptionDescriptor.Mode.User)
                            .withMinMaxValues(1,1),
                    new OptionDescriptor<>(
                            String.class, StepDefinitionConstants.SdfUpload.OPTION_NAME_FIELD_NAME,
                            "Name field name",
                            "Name of the field to use for the molecule name (the part before the CTAB block)",
                            OptionDescriptor.Mode.User)
                            .withMinMaxValues(0,1)

            },
            null, null, null,
            SDFReaderStep.class.getName()
    );

    /**
     * How to handle the name field (the first line of the CTAB block). See
     * {@link SDFReader} for details.
     */
    public static final String OPTION_NAME_FIELD_NAME = StepDefinitionConstants.SdfUpload.OPTION_NAME_FIELD_NAME;


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
    public Map<String, Object> doExecute(Map<String, Object> inputs) throws Exception {
        statusMessage = "Reading SDF ...";
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Must provide a single input. Found " + inputs.size());
        }
        Object input = inputs.values().iterator().next();
        SquonkDataSource dataSource;
        if (input instanceof SquonkDataSource) {
            dataSource = (SquonkDataSource) input;
        } else if (input instanceof InputStream) {
            dataSource = new InputStreamDataSource(SquonkDataSource.ROLE_DEFAULT, null, CommonMimeTypes.MIME_TYPE_MDL_SDF, (InputStream) input, null);
        } else if (input instanceof SDFile) {
            dataSource = ((SDFile)input).getDataSources()[0];
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
        mols = addStreamCounter(mols, "%s molecules read");
        Dataset results = new Dataset(mols, reader.getDatasetMetadata());
        return Collections.singletonMap("output", results);
    }

}
