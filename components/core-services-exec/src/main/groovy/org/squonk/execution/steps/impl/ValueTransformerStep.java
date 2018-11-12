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
import org.squonk.camel.processor.ValueTransformerProcessor;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.transform.TransformDefinitions;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.io.JsonHandler;

import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ValueTransformerStep extends AbstractDatasetStep {

    private static final Logger LOG = Logger.getLogger(ValueTransformerStep.class.getName());


    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor(
            "core.dataset.transformvalues.v1",
            "TransformValues",
            "Transform dataset values",
            new String[]{"transform", "convert", "dataset"},
            null, "icons/transform_basic.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{

                    new OptionDescriptor<>(new MultiLineTextTypeDescriptor(10, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                            StepDefinitionConstants.ValueTransformer.OPTION_TRANSFORMS, "Transform Definitions",
                            "Definition of the transforms to perform", OptionDescriptor.Mode.User)
            },
            null, null, null,
            ValueTransformerStep.class.getName()
    );

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_TRANSFORMS = StepDefinitionConstants.ValueTransformer.OPTION_TRANSFORMS;

    /**
     * Add the transforms to the dataset Stream. NOTE: transforms will not occur
     * until a terminal operation is performed on the Stream. Normally no output is
     * created as the transforms are added to the input dataset which will be
     * transient, however if an output field is needed then specify a mapping for the
     * field named FIELD_OUTPUT_DATASET.
     *
     * @param input
     * @param camelContext
     * @throws Exception
     */
    @Override
    protected Dataset doExecuteWithDataset(Dataset input, CamelContext camelContext) throws Exception {
        Object val = getOption(OPTION_TRANSFORMS);
        if (val == null) {
            throw new IllegalStateException("Transforms must be defined as option named " + OPTION_TRANSFORMS);
        }

        TransformDefinitions txs = null;
        if (val instanceof TransformDefinitions) {
            txs = (TransformDefinitions)val;
        } else { // otherwise it must be json representing TransformDefinitions
            txs = JsonHandler.getInstance().objectFromJson(val.toString(), TransformDefinitions.class);
        }
        LOG.info("Transform Definitions: " + txs);
        statusMessage = "Transforming dataset ...";
        ValueTransformerProcessor p = ValueTransformerProcessor.create(txs);
        Dataset result = p.execute(camelContext.getTypeConverter(), input);
        return result;

    }

}
