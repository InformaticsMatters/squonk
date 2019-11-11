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

import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.TypesUtils;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Converts a Dataset&lt;BasicObject&gt; to a Dataset&lt;MoleculeObject&gt;
 * given a specified field name that contains the structure. A default name of
 * "structure" is used if option is not specified. The field name is handled in
 * a case insensitive manner.
 *
 * NOTE: if a field with that name is not found MoleculeObjects with an empty
 * structure is generated.
 *
 * NOTE: does not perform any validation of the structure - if bad structures
 * are present errors will occur later when these are parsed.
 *
 * NOTE: This step generates a new Stream that contains the functions necessary
 * to convert the BasicObjects to the MoleculeObjects and creates a new Dataset
 * with that new Stream, but it does NOT call a terminal operation on the Stream
 * to actually do the conversions. You are responsible for providing that
 * terminal operation on the output Dataset.
 *
 * @author timbo
 */
public class BasicObjectToMoleculeObjectStep extends AbstractDatasetStep<BasicObject, MoleculeObject> {

    private static final Logger LOG = Logger.getLogger(BasicObjectToMoleculeObjectStep.class.getName());

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor(
            "core.dataset.basictomolecule.v1",
            "ConvertToMolecules",
            "Convert BasicObjects To MoleculeObjects using data from one of the fields",
            new String[]{"dataset", "convert", "transform", "structures", "molecules"},
            null, "icons/transform_basic_to_molecule.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{

                    new OptionDescriptor<>(new DatasetFieldTypeDescriptor(new Class[] {String.class}),
                            "structureFieldName", "Structure Field Name",
                            "Name of property to use for the structure",
                            OptionDescriptor.Mode.User),
                    new OptionDescriptor<>(String.class, "structureFormat",
                            "Structure Format",
                            "Format of the structures e.g. smiles, mol",
                            OptionDescriptor.Mode.User)
                            .withValues(new String[]{"smiles", "mol"}),
                    new OptionDescriptor<>(Boolean.class, "preserveUuid",
                            "Preserve UUID",
                            "Keep the existing UUID or generate a new one",
                            OptionDescriptor.Mode.User)
                            .withMinMaxValues(1,1)
                            .withDefaultValue(true)

            },
            null, null, null,
            BasicObjectToMoleculeObjectStep.class.getName()
    );

    public static final String OPTION_STRUCTURE_FIELD_NAME = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_STRUCTURE_FIELD_NAME;
    public static final String OPTION_STRUCTURE_FORMAT = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_STRUCTURE_FORMAT;
    public static final String OPTION_PRESERVE_UUID = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_PRESERVE_UUID;

    public static String DEFAULT_STRUCTURE_FIELD_NAME = "structure";


    @Override
    protected Dataset<MoleculeObject> doExecuteWithDataset(Dataset<BasicObject> input) throws Exception {

        TypeConverter converter = findTypeConverter();
        String structureFieldName = getOption(OPTION_STRUCTURE_FIELD_NAME, String.class, converter, DEFAULT_STRUCTURE_FIELD_NAME);
        String structureFormat = getOption(OPTION_STRUCTURE_FORMAT, String.class, converter);
        boolean preserveUuid = getOption(OPTION_PRESERVE_UUID, Boolean.class, converter, true);

        statusMessage = "Applying conversions ...";
        Dataset<MoleculeObject> results = TypesUtils.convertBasicObjectDatasetToMoleculeObjectDataset(input, structureFieldName, structureFormat, preserveUuid);
        return results;
    }

}
