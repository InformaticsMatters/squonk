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
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStandardStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class DatasetSelectRandomStep extends AbstractStandardStep {

    private static final Logger LOG = Logger.getLogger(DatasetSelectRandomStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_RANDOM = StepDefinitionConstants.DatasetSelectRandom.OPTION_RANDOM;
    public static final String OPTION_COUNT = StepDefinitionConstants.DatasetSelectRandom.OPTION_COUNT;

    /**
     * Create a random subset of the dataset up to a maximun (count option, default 1000) number of values selecting items from the start
     * on random based on a random probability (random option, default 0.001) of selection.
     *
     * @param varman
     * @param context
     * @throws Exception
     */
    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;
        Dataset ds = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman);
        if (ds == null) {
            throw new IllegalStateException("Input variable not found: " + VAR_INPUT_DATASET);
        }
        LOG.fine("Input Dataset: " + ds);

        Float randomOpt = getOption(OPTION_RANDOM, Float.class, context.getTypeConverter());
        Integer countOpt = getOption(OPTION_COUNT, Integer.class, context.getTypeConverter());
        float random = randomOpt == null ? 0.001f : randomOpt;
        int count = countOpt == null ? 1000 : countOpt;

        statusMessage = "Setting filter ...";
        Random g = new Random();
        Stream stream = (Stream)ds.getStream().sequential();
        stream = stream.filter(o -> random < g.nextFloat()).limit(count);

        DatasetMetadata meta = ds.getMetadata();
        meta.setSize(0); // will be recalculated
        Dataset results = new Dataset(stream, meta);
        String outFldName = mapOutputVariable(VAR_OUTPUT_DATASET);
        if (outFldName != null) {
            createVariable(outFldName, Dataset.class, results, varman);
        }

        statusMessage = generateStatusMessage(ds.getSize(), results.getSize(), -1);
        LOG.info("Results: " + ds.getMetadata());
    }

}