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
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;
import org.squonk.options.DatasetFieldTypeDescriptor;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sorts a dataset according to a single field.
 * <p>
 * Created by timbo on 16/06/2020.
 */
public class SimpleSorterStep<P extends BasicObject> extends AbstractSorterStep<P> {

    private static final Logger LOG = Logger.getLogger(SimpleSorterStep.class.getName());

    public static final String OPTION_FIELD = StepDefinitionConstants.SimpleSorter.OPTION_FIELD;
    public static final String OPTION_ASC = StepDefinitionConstants.SimpleSorter.OPTION_ASC;

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.simplesorter.v1",
            "Simple sorter",
            "Sort the dataset based on a field's values",
            new String[]{"sort", "dataset"},
            null, "icons/filter.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(VAR_INPUT_DATASET),
            IODescriptors.createBasicObjectDatasetArray(VAR_OUTPUT_DATASET),
            new OptionDescriptor[]{
                    new OptionDescriptor<>(new DatasetFieldTypeDescriptor(null),
                            OPTION_FIELD, "Field to sort with",
                            "Name of field whose values are used to sort the dataset",
                            OptionDescriptor.Mode.User),
                    new OptionDescriptor<>(Boolean.class, OPTION_ASC, "Sort ascending",
                            "Sort the data in ascending order", OptionDescriptor.Mode.User)
                            .withDefaultValue(Boolean.TRUE)
            },
            null, null, null,
            SimpleSorterStep.class.getName()
    );

    @Override
    protected Dataset<P> doExecuteWithDataset(Dataset<P> input) throws Exception {

        String sortField = getOption(OPTION_FIELD, String.class, findTypeConverter());
        if (sortField == null) {
            throw new IllegalStateException("Sort field must be defined as option named " + OPTION_FIELD);
        }
        Boolean ascending = getOption(OPTION_ASC, Boolean.class, findTypeConverter());
        if (ascending == null) {
            ascending = true;
        }

        DatasetMetadata<P> meta = input.getMetadata();
        SortDirective sortDirective = new SortDirective(sortField, ascending);
        Stream<P> sorted = doSort(input.getStream(), Collections.singletonList(sortDirective));
        meta.appendDatasetHistory("Sorted according to " + sortField + (ascending ? " ASC" : " DESC"));
        Dataset<P> result = new Dataset(sorted, meta);
        return result;
    }

}
