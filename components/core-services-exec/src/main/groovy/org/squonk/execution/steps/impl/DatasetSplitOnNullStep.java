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
import org.squonk.execution.steps.AbstractStep;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;
import org.squonk.util.CommonMimeTypes;
import org.squonk.options.DatasetFieldTypeDescriptor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 29/12/15.
 */
public class DatasetSplitOnNullStep<P extends BasicObject> extends AbstractDatasetSplitStep<P> {

    private static final Logger LOG = Logger.getLogger(DatasetSplitOnNullStep.class.getName());

    public static final String OPTION_FIELD = StepDefinitionConstants.DatasetSplitOnNull.OPTION_FIELD;


    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor(
            "core.dataset.splitonnull.v1",
            "DatasetSplitOnNull",
            "Split a dataset based on whether values for a field are present or not",
            new String[]{"filter", "split", "dataset"},
            null, "icons/filter.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            new IODescriptor[]{
                    IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_OUTPUT_PASS),
                    IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_OUTPUT_FAIL)
            },
            new OptionDescriptor[]{
                    new OptionDescriptor<>(new DatasetFieldTypeDescriptor(null),
                            OPTION_FIELD, "Field to inspect",
                            "Name of field whose values are used to split the dataset",
                            OptionDescriptor.Mode.User)

            },
            null, null, null,
            DatasetSplitOnNullStep.class.getName()
    );

    protected Map<String, Object> doExecuteWithDataset(Dataset<P> input) throws Exception {

        TypeConverter converter = findTypeConverter();
        String fieldName = getOption(OPTION_FIELD, String.class, converter);
        LOG.info("Splitting on nulls for field: " + fieldName);

        statusMessage = "Splitting ...";
        Map<Boolean, List<P>> groups = input.getStream()
                .collect(Collectors.partitioningBy(mo -> {
                    Object value = mo.getValue(fieldName);
                    if (value == null) {
                        return false;
                    }
                    if (value instanceof String) {
                        String s = (String)value;
                        return !s.isEmpty();
                    } else {
                        return true;
                    }
                }));

        statusMessage = groups.get(true).size() + " present and " + groups.get(false).size() + " absent";

        LOG.info("Number present=" + groups.get(true).size());
        LOG.info("Number absent =" + groups.get(false).size());

        DatasetMetadata presentMeta = input.getMetadata().clone();
        DatasetMetadata absentMeta = input.getMetadata().clone();

        Map<String, Object> results = new LinkedHashMap(2);
        results.put(StepDefinitionConstants.VARIABLE_OUTPUT_PASS, new Dataset(groups.get(true), presentMeta));
        results.put(StepDefinitionConstants.VARIABLE_OUTPUT_FAIL, new Dataset(groups.get(false), absentMeta));

        return results;
    }


}
