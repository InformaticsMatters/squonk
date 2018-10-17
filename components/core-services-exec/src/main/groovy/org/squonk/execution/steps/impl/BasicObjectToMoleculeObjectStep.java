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
import org.apache.camel.TypeConverter;
import org.squonk.types.BasicObject;
import org.squonk.types.TypesUtils;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.MoleculeObject;
import org.squonk.dataset.Dataset;

import java.util.Map;
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

    public static final String OPTION_STRUCTURE_FIELD_NAME = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_STRUCTURE_FIELD_NAME;
    public static final String OPTION_STRUCTURE_FORMAT = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_STRUCTURE_FORMAT;
    public static final String OPTION_PRESERVE_UUID = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_PRESERVE_UUID;

    public static String DEFAULT_STRUCTURE_FIELD_NAME = "structure";


    @Override
    protected Dataset<MoleculeObject> doExecuteWithDataset(Dataset<BasicObject> input, CamelContext context) throws Exception {

        TypeConverter converter = findTypeConverter(context);
        String structureFieldName = getOption(OPTION_STRUCTURE_FIELD_NAME, String.class, converter, DEFAULT_STRUCTURE_FIELD_NAME);
        String structureFormat = getOption(OPTION_STRUCTURE_FORMAT, String.class, converter);
        boolean preserveUuid = getOption(OPTION_PRESERVE_UUID, Boolean.class, converter, true);

        statusMessage = "Applying conversions ...";
        Dataset<MoleculeObject> results = TypesUtils.convertBasicObjectDatasetToMoleculeObjectDataset(input, structureFieldName, structureFormat, preserveUuid);
        return results;
    }

}
