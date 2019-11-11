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

import org.apache.camel.TypeConverter;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Allows to create a new Dataset from the molecules in a specified field. That field must be of type MoleculeObject[]
 * The resulting dataset contains all the molecules that were found for that field in all the records of the dataset
 *
 * @author timbo
 */
public class DatasetMoleculesFromFieldStep<P extends BasicObject> extends AbstractDatasetStep<P,MoleculeObject> {

    private static final Logger LOG = Logger.getLogger(DatasetMoleculesFromFieldStep.class.getName());

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.moleculesfromfield.v1",
            "MoleculesFromField",
            "Create a new datset from all the molecules from a field",
            new String[]{"molecule", "extractor", "flatmap", "dataset"},
            null, "icons/molecule_generator.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{

                    new OptionDescriptor<>(new DatasetFieldTypeDescriptor(new Class[] {MoleculeObject[].class}),
                            StepDefinitionConstants.DatasetMoleculesFromFieldStep.OPTION_MOLECULES_FIELD,
                            "Field with molecules",
                            "Field that contains an Array of MoleculeObjects",
                            OptionDescriptor.Mode.User)
                            .withMinMaxValues(1,1)

            },
            null, null, null,
            DatasetMoleculesFromFieldStep.class.getName()
    );

    public static final String OPTION_MOLECULES_FIELD = StepDefinitionConstants.DatasetMoleculesFromFieldStep.OPTION_MOLECULES_FIELD;
    public static final String FIELD_NAME_ORIGIN = "MoleculeSource";

    /**
     *
     * @param input
     * @throws Exception
     */
    @Override
    protected Dataset<MoleculeObject> doExecuteWithDataset(Dataset<P> input) throws Exception {

        TypeConverter converter = findTypeConverter();
        String fieldName = getOption(OPTION_MOLECULES_FIELD, String.class, converter);
        if (fieldName == null) {
            throw new IllegalStateException("Selected field not found. Option named " + OPTION_MOLECULES_FIELD + " must present");
        }
        LOG.info("Input fieldName: " + fieldName);


        statusMessage = "Setting molecule extractor ...";
        Stream<MoleculeObject> stream = input.getStream()
                .sequential()
                .flatMap((mo) -> {
                    UUID uuid = mo.getUUID();
                    MoleculeObject[] mols = mo.getValue(fieldName, MoleculeObject[].class);
                    if (mols == null) {
                        return Stream.empty();
                    } else {
                        return Arrays.stream(mols).peek(mol -> mol.putValue(FIELD_NAME_ORIGIN, uuid));
                    }
                });

        DatasetMetadata<P> origMeta = input.getMetadata();
        DatasetMetadata<MoleculeObject> meta = new DatasetMetadata<>(MoleculeObject.class);
        meta.appendDatasetHistory("Dataset created molecules from field " + fieldName + " of source dataset: " + origMeta.getProperty(DatasetMetadata.PROP_DESCRIPTION));
        meta.createField(FIELD_NAME_ORIGIN, this.getClass().getSimpleName() , "UUID of source molecule", UUID.class);
        Dataset<MoleculeObject> results = new Dataset(stream, meta);

        statusMessage = generateStatusMessage(input.getSize(), results.getSize(), -1);
        return results;
    }

}
