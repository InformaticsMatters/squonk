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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Remove fields from a dataset
 * <p>
 * Created by timbo on 22/06/2020.
 */
public class DatasetFieldFilterStep<P extends BasicObject> extends AbstractDatasetSplitStep<P> {

    private static final Logger LOG = Logger.getLogger(DatasetFieldFilterStep.class.getName());

    public static final String OPTION_FIELDS = StepDefinitionConstants.DatasetFieldFilter.OPTION_FIELDS;


    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor(
            "core.dataset.fieldfilter.v1",
            "DatasetFieldFilter",
            "Remove unwanted fields from a dataset",
            new String[]{"filter", "dataset"},
            null, "icons/filter.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{
                    new OptionDescriptor<>(new DatasetFieldTypeDescriptor(StepDefinitionConstants.VARIABLE_INPUT_DATASET, null, true),
                            OPTION_FIELDS, "Fields to keep",
                            "Name of fields in the dataset to keep",
                            OptionDescriptor.Mode.User)

            },
            null, null, null,
            DatasetFieldFilterStep.class.getName()
    );


    protected Map<String, Object> doExecuteWithDataset(Dataset<P> input) throws Exception {

        TypeConverter converter = findTypeConverter();

        List<String> fields = getOption(OPTION_FIELDS, List.class, converter);
        LOG.info("Keeping fields: " + fields);

        statusMessage = "Filtering ...";
        AtomicInteger count = new AtomicInteger(0);
        Stream<P> stream = input.getStream().peek(bo -> {
            Iterator<Map.Entry<String,Object>> it = bo.getValues().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String,Object> e = it.next();
                if (!fields.contains(e.getKey())) {
                    it.remove();
                    count.incrementAndGet();
                }
            }
        }).onClose(() -> {
            statusMessage = count.intValue() + " values removed";
            LOG.info(count.intValue() + " values removed");
        });

        DatasetMetadata resultMeta = input.getMetadata().clone();
        resultMeta.getFieldMetaPropsMap().keySet().removeIf(k -> !fields.contains(k));
        resultMeta.getValueClassMappings().keySet().removeIf(k -> !fields.contains(k));
        resultMeta.appendDatasetHistory("Removed fields other than " + fields.stream().collect(Collectors.joining(",")));

        Map<String, Object> results = new LinkedHashMap(1);
        results.put(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, new Dataset(stream, resultMeta));

        return results;
    }

}
