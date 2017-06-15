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
import org.squonk.camel.processor.ValueTransformerProcessor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.transform.AbstractTransform;
import org.squonk.dataset.transform.Message;
import org.squonk.dataset.transform.PotionParser;
import org.squonk.dataset.transform.TransformDefinitions;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author timbo
 */
public class DataTransformPotionStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DataTransformPotionStep.class.getName());

    public static final String OPTION_POTION = StepDefinitionConstants.DataTransformPotion.OPTION_POTION;

    /**
     * Add the transforms to the dataset Stream. NOTE: transforms will not occur
     * until a terminal operation is performed on the Stream. Normally no output is
     * created as the transforms are added to the input dataset which will be
     * transient, however if an output field is needed then specify a mapping for the 
     * field named FIELD_OUTPUT_DATASET. 
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

        String potion = getOption(OPTION_POTION, String.class);
        if (potion == null) {
            throw new IllegalStateException("Potion must be defined as option named " + OPTION_POTION);
        }

        PotionParser parser = TransformDefinitions.parse(potion, ds.getMetadata().getValueClassMappings());
        if (parser.getErrorCount() > 0) {
            statusMessage = parser.getMessages().stream()
                    .filter(m -> m.getLevel() == Message.Severity.Error)
                    .map(m -> m.toString())
                    .collect(Collectors.joining("\n"));
            // TODO - how best to notify user?
            return;
        }

        List<AbstractTransform> list = parser.getTransforms();
        if (list.isEmpty()) {
            statusMessage = "No transform definitions found";
            return;
        }
        TransformDefinitions txs = new TransformDefinitions(list);
        LOG.info("Transform Definitions: " + txs);
        statusMessage = "Transforming dataset ...";
        ValueTransformerProcessor p = ValueTransformerProcessor.create(txs);
        Dataset result = p.execute(context.getTypeConverter(), ds);

        LOG.info("Transforms defined");

        createMappedOutput("output", Dataset.class, result, varman);

        statusMessage = generateStatusMessage(ds.getSize(), result.getSize(), -1);
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(result.getMetadata()));
    }

}
