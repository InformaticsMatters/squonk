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
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Split a dataset based on a fraction of rows or a number of rows.
 * How the dataset is split is determined by the value of the OPTION_FRAC option. If the value is between 0 and 1 it is
 * treated as a fraction of the dataset to put in the 'pass' group. If it is a value greater than 1 it is treated as
 * the number of records to put it the pass group. In both cases the remaining records are put in the 'fail' group.
 * The OPTION_RANDOMISE option specifies whether to pick the records randomly (true) or sequentially (false).
 *
 * The entire dataset is held in memory during execution so do not use for very large datasets.
 * <p>
 * Created by timbo on 22/06/2020.
 */
public class DatasetSplitStep<P extends BasicObject> extends AbstractDatasetSplitStep<P> {

    private static final Logger LOG = Logger.getLogger(DatasetSplitStep.class.getName());

    public static final String OPTION_FRAC = StepDefinitionConstants.DatasetSplit.OPTION_FRAC;
    public static final String OPTION_RANDOMISE = StepDefinitionConstants.DatasetSplit.OPTION_RANDOMISE;


    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor(
            "core.dataset.split.v1",
            "DatasetSplit",
            "Split a dataset based on a fraction of rows or a number of rows",
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
                    new OptionDescriptor<>(Float.class, OPTION_FRAC,
                            "Count or fraction", "Number (integer) or fraction (0-1) of rows", OptionDescriptor.Mode.User)
                            .withMinMaxValues(1, 1),
                    new OptionDescriptor<>(Boolean.class, OPTION_RANDOMISE,
                            "Randomise", "Sequential or random selection of rows", OptionDescriptor.Mode.User)
            },
            null, null, null,
            DatasetSplitStep.class.getName()
    );


    protected Map<String, Object> doExecuteWithDataset(Dataset<P> input) throws Exception {

        TypeConverter converter = findTypeConverter();
        Float frac = getOption(OPTION_FRAC, Float.class, converter);
        if (frac == null) {
            throw new IllegalArgumentException("Must specify option " + OPTION_FRAC);
        }
        if (frac < 0) {
            throw new IllegalArgumentException(OPTION_FRAC + " must be greater than zero");
        }
        boolean rand = getOption(OPTION_RANDOMISE, Boolean.class, converter, false);
        LOG.info("Splitting count/frac using: " + frac + " randomise: " + rand);

        statusMessage = "Splitting ...";
        List<P> items = input.getStream().collect(Collectors.toList());

        List<P> pass = null;
        List<P> fail = null;
        if (frac < 1) {
            if (rand) {
                Random g = new Random();
                pass = new ArrayList<>();
                int count = Math.round((float)items.size() * frac);
                for (int i=0; i < count; i++) {
                    int idx = g.nextInt(items.size());
                    pass.add(items.remove(idx));
                }
                fail = items;
            } else {
                float sizef = (float)items.size();
                int cuttoff = Math.round(sizef * frac);
                pass = items.subList(0, cuttoff);
                fail = items.subList(cuttoff, items.size());
            }
        } else {
            int cuttoff = Math.round(frac);
            if (rand) {
                Random g = new Random();
                int count = Math.round(frac);
                pass = new ArrayList<>();
                for (int i=0; i < count; i++) {
                    int idx = g.nextInt(items.size());
                    pass.add(items.remove(idx));
                }
                fail = items;
            } else {
                if (cuttoff > items.size()) {
                    pass = items;
                    fail = Collections.emptyList();
                } else {
                    pass = items.subList(0, cuttoff);
                    fail = items.subList(cuttoff, items.size());
                }
            }
        }

        statusMessage = "Split into " + pass.size() + " and " + fail.size() + " records";

        LOG.info("Number pass=" + pass.size());
        LOG.info("Number fail=" + fail.size());

        DatasetMetadata passMeta = input.getMetadata().clone();
        DatasetMetadata failMeta = input.getMetadata().clone();
        passMeta.setSize(pass.size());
        failMeta.setSize(fail.size());

        Map<String, Object> results = new LinkedHashMap(2);
        results.put(StepDefinitionConstants.VARIABLE_OUTPUT_PASS, new Dataset(pass, passMeta));
        results.put(StepDefinitionConstants.VARIABLE_OUTPUT_FAIL, new Dataset(fail, failMeta));

        return results;
    }


}
