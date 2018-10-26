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
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class DatasetSelectRandomStep<P extends BasicObject> extends AbstractDatasetStep<P,P> {

    private static final Logger LOG = Logger.getLogger(DatasetSelectRandomStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_RANDOM = StepDefinitionConstants.DatasetSelectRandom.OPTION_RANDOM;
    public static final String OPTION_COUNT = StepDefinitionConstants.DatasetSelectRandom.OPTION_COUNT;


    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.filter.random.v1", "Dataset random selector", "Generate a random slice of the dataset",
            new String[]{"filter", "random", "dataset"},
            null, "icons/filter.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{
                    new OptionDescriptor<>(Float.class, StepDefinitionConstants.DatasetSelectRandom.OPTION_RANDOM, "Random fraction (0-1)", "The fraction of records to randomly select (between 0 and 1, default 0.001)", OptionDescriptor.Mode.User),
                    new OptionDescriptor<>(Integer.class, StepDefinitionConstants.DatasetSelectRandom.OPTION_COUNT, "Max records", "The max number of records to include, default 1000", OptionDescriptor.Mode.User)
            },
            null, null, null,
            DatasetSelectRandomStep.class.getName()
    );

    /** Create a random subset of the dataset up to a maximum (count option, default 1000) number of values selecting items from the start
     * on random based on a random probability (random option, default 0.001) of selection.
     *
     * @param input
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    protected Dataset<P> doExecuteWithDataset(Dataset<P> input, CamelContext context) throws Exception {

        TypeConverter converter = findTypeConverter(context);
        Float randomOpt = getOption(OPTION_RANDOM, Float.class, converter);
        Integer countOpt = getOption(OPTION_COUNT, Integer.class, converter);
        float random = randomOpt == null ? 0.001f : randomOpt;
        int count = countOpt == null ? 1000 : countOpt;

        statusMessage = "Setting filter ...";
        Random g = new Random();
        Stream<P> stream = input.getStream().sequential();
        stream = stream.filter(o -> random < g.nextFloat()).limit(count);
        stream = addStreamCounter(stream, "%s records selected");

        DatasetMetadata<P> meta = input.getMetadata();
        meta.setSize(0); // will be recalculated

        return new Dataset<>(stream, meta);
    }

}