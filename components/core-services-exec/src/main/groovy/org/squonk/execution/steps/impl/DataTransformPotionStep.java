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
import org.squonk.camel.processor.ValueTransformerProcessor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.transform.AbstractTransform;
import org.squonk.dataset.transform.Message;
import org.squonk.dataset.transform.PotionParser;
import org.squonk.dataset.transform.TransformDefinitions;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author timbo
 */
public class DataTransformPotionStep<P extends BasicObject> extends AbstractDatasetStep<P,P> {

    private static final Logger LOG = Logger.getLogger(DataTransformPotionStep.class.getName());

    public static final String OPTION_POTION = StepDefinitionConstants.DataTransformPotion.OPTION_POTION;

    /**
     * Add the transforms to the dataset Stream. NOTE: transforms will not occur
     * until a terminal operation is performed on the Stream. Normally no output is
     * created as the transforms are added to the input dataset which will be
     * transient, however if an output field is needed then specify a mapping for the 
     * field named FIELD_OUTPUT_DATASET.
     *
     * @param input
     * @throws Exception
     */
    protected Dataset<P> doExecuteWithDataset(Dataset<P> input) throws Exception {

        TypeConverter converter = findTypeConverter();
        String potion = getOption(OPTION_POTION, String.class, converter);
        if (potion == null) {
            throw new IllegalStateException("Potion must be defined as option named " + OPTION_POTION);
        }

        PotionParser parser = TransformDefinitions.parse(potion, input.getMetadata().getValueClassMappings());
        if (parser.getErrorCount() > 0) {
            statusMessage = parser.getMessages().stream()
                    .filter(m -> m.getLevel() == Message.Severity.Error)
                    .map(m -> m.toString())
                    .collect(Collectors.joining("\n"));
            throw new IllegalArgumentException("Failed to parse potion: " + parser.getMessages().stream()
                    .map(Message::toString)
                    .collect(Collectors.joining(", ")));
        }

        List<AbstractTransform> list = parser.getTransforms();
        if (list.isEmpty()) {
            statusMessage = "No transform definitions found";
            throw new IllegalArgumentException("No transform definitions found");
        }
        TransformDefinitions txs = new TransformDefinitions(list);
        LOG.info("Transform Definitions: " + txs);
        statusMessage = "Transforming dataset ...";
        ValueTransformerProcessor p = ValueTransformerProcessor.create(txs);
        Dataset<P> result = p.execute(converter, input);

        LOG.info("Transforms defined");

        statusMessage = generateStatusMessage(input.getSize(), result.getSize(), -1);
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(result.getMetadata()));
        return result;
    }

}
