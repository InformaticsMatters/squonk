/*
 * Copyright (c) 2020 Informatics Matters Ltd.
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
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Sorts a dataset according to one or more sort directives.
 * Note: valid DatasetMetadata must be present in the dataset.
 *
 * Created by timbo on 13/09/16.
 */
public class DatasetSorterStep<P extends BasicObject> extends AbstractSorterStep<P> {

    private static final Logger LOG = Logger.getLogger(DatasetSorterStep.class.getName());

    public static final String OPTION_DIRECTIVES = StepDefinitionConstants.DatasetSorter.OPTION_DIRECTIVES;

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.sorter.v1",
            "Dataset sorter",
            "Sort the dataset",
            new String[]{"sort", "dataset"},
            null, "icons/filter.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(VAR_INPUT_DATASET),
            IODescriptors.createBasicObjectDatasetArray(VAR_OUTPUT_DATASET),
            new OptionDescriptor[]{

                    new OptionDescriptor<>(
                            new MultiLineTextTypeDescriptor(10, 80, MultiLineTextTypeDescriptor.MIME_TYPE_TEXT_PLAIN),
                            StepDefinitionConstants.DatasetSorter.OPTION_DIRECTIVES,
                            "Sort directives",
                            "Definition of the sort directives: field_name ASC|DESC", OptionDescriptor.Mode.User)
                            .withMinMaxValues(1,1)

            },
            null, null, null,
            DatasetSorterStep.class.getName()
    );

    @Override
    protected Dataset<P> doExecuteWithDataset(Dataset<P> input) throws Exception {

        String directivesStr = getOption(OPTION_DIRECTIVES, String.class, findTypeConverter());
        if (directivesStr == null) {
            throw new IllegalStateException("Sort directives must be defined as option named " + OPTION_DIRECTIVES);
        }

        DatasetMetadata<P> meta = input.getMetadata();
        List<SortDirective> directives = parse(directivesStr, meta);

        Stream<P> sorted = doSort(input.getStream(), directives);

        meta.appendDatasetHistory("Sorted according to " + directives.stream()
                .map((sd) -> sd.field + (sd.ascending ? " ASC" : " DESC"))
                .collect(Collectors.joining(", ")));

        Dataset<P> result = new Dataset(sorted, meta);

        return result;
    }

    protected List<SortDirective> parse(String string, DatasetMetadata meta) {
        List<SortDirective> list = new ArrayList<>();
        String[] lines = string.split("\n|( *, *)");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            SortDirective sd = null;
            if (parts.length == 1) {
                sd = new SortDirective(parts[0], true);
            } else if (parts.length == 2) {
                if ("ASC".equalsIgnoreCase(parts[1])) {
                    sd = new SortDirective(parts[0], true);
                } else if ("DESC".equalsIgnoreCase(parts[1])) {
                    sd = new SortDirective(parts[0], false);
                } else {
                    throw new IllegalArgumentException("Invalid sort directive. Expected ASC or DESC but found: " + parts[1]);
                }
            } else {
                throw new IllegalArgumentException("Invalid sort expression: " + line);
            }
            if (sd != null) {
                Class type = (Class)meta.getValueClassMappings().get(sd.field);
                if (type == null) {
                    throw new IllegalArgumentException("Invalid field name: " + sd.field);
                }
                if (!Comparable.class.isAssignableFrom(type)) {
                    throw new IllegalArgumentException("Field type is not Comparable: " + type.getName());
                }
                list.add(sd);
            }
        }

        return list;
    }
}
