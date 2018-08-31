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

import org.apache.camel.TypeConverter;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;

import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author timbo
 */
public class DatasetSelectSliceStep<P extends BasicObject> extends AbstractDatasetStandardStep<P,P> {

    private static final Logger LOG = Logger.getLogger(DatasetSelectSliceStep.class.getName());

    public static final String OPTION_SKIP = StepDefinitionConstants.DatasetSelectSlice.OPTION_SKIP;
    public static final String OPTION_COUNT = StepDefinitionConstants.DatasetSelectSlice.OPTION_COUNT;

    public static DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.filter.slice.v1", "Dataset slice selector", "Generate a defined slice of the dataset",
            new String[]{"filter", "slice", "dataset"},
            null, "icons/filter.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{
                    new OptionDescriptor<>(Integer.class, StepDefinitionConstants.DatasetSelectSlice.OPTION_SKIP, "Number to skip", "The number of records to skip", OptionDescriptor.Mode.User),
                    new OptionDescriptor<>(Integer.class, StepDefinitionConstants.DatasetSelectSlice.OPTION_COUNT, "Number to include", "The number of records to include after skipping", OptionDescriptor.Mode.User)
            },
            null, null, null,
            DatasetSelectSliceStep.class.getName()
    );

    /**
     * Create a slice of the dataset skipping a number of records specified by the skip option (or 0 if not specified)
     * and including only the number of records specified by the count option (or till the end if not specified).
     *
     * @param input The dataset to slice
     * @throws Exception
     */
    @Override
    protected Dataset<P> doExecute(Dataset<P> input, Map<String,Object> options, TypeConverter converter) throws Exception {

        int skip = getOption(options, OPTION_SKIP, Integer.class, converter, 0);
        Integer count = getOption(options, OPTION_COUNT, Integer.class, converter);

        statusMessage = "Setting filters ...";
        Stream<P> stream = input.getStream().sequential();
        if (skip > 0) {
            LOG.info("Setting skip to " + skip);
            stream = stream.skip(skip);
        }
        if (count != null) {
            LOG.info("Setting count to " + count);
            stream = stream.limit(count);
        }
        DatasetMetadata<P> meta = input.getMetadata();

        int total = -1;
        if (count == null) {
            int i = input.getSize();
            if (i > 0) {
                // assume this value is correct
                total = Math.max(i - skip, 0);
            } else {
                // we can't tell how many
            }
        } else {
            total = count;
        }

        meta.setSize(total); // will be recalculated

        statusMessage = generateStatusMessage(input.getSize(), total, -1);

        return new Dataset<>(stream, meta);
    }

}
