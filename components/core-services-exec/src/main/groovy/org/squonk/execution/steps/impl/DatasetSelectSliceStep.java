/*
 * Copyright (c) 2017 Informatics Matters Ltd.
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
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractServiceStep;
import org.squonk.execution.steps.AbstractStandardStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;

import java.util.Date;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class DatasetSelectSliceStep extends AbstractServiceStep {

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
     * and including only the number of records specified by the count option (or till teh end if not specified).
     *
     * @param varman
     * @param context
     * @throws Exception
     */
    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;
        Dataset ds = fetchMappedInput("input", Dataset.class, varman);
        if (ds == null) {
            throw new IllegalStateException("Input variable not found: input");
        }
        LOG.fine("Input Dataset: " + ds);


        Integer skip = getOption(OPTION_SKIP, Integer.class);
        Integer count = getOption(OPTION_COUNT, Integer.class);

        statusMessage = "Setting filters ...";
        Stream stream = (Stream)ds.getStream().sequential();
        if (skip != null) {
            LOG.info("Setting skip to " + skip);
            stream = stream.skip(skip);
        }
        if (count != null) {
            LOG.info("Setting count to " + count);
            stream = stream.limit(count);
        }
        DatasetMetadata meta = ds.getMetadata();
        meta.setSize(0); // will be recalculated

        Dataset results = new Dataset(stream, meta);
        
        String outFldName = mapOutputVariable("output");
        if (outFldName != null) {
            createVariable(outFldName, Dataset.class, results, varman);
        }

        statusMessage = generateStatusMessage(ds.getSize(), results.getSize(), -1);
        LOG.info("Results: " + ds.getMetadata());
    }

}
