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
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.MoleculeObject;
import org.squonk.util.MoleculeObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class SmilesDeduplicatorStep extends AbstractDatasetStep {

    private static final Logger LOG = Logger.getLogger(SmilesDeduplicatorStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_CANONICAL_SMILES_FIELD = StepDefinitionConstants.SmilesDeduplicator.OPTION_CANONICAL_SMILES_FIELD;
    public static final String OPTION_KEEP_FIRST_FIELDS = StepDefinitionConstants.SmilesDeduplicator.OPTION_KEEP_FIRST_FIELDS;
    public static final String OPTION_KEEP_LAST_FIELDS = StepDefinitionConstants.SmilesDeduplicator.OPTION_KEEP_LAST_FIELDS;
    public static final String OPTION_APPEND_FIELDS = StepDefinitionConstants.SmilesDeduplicator.OPTION_APPEND_FIELDS;

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
        String canonicalSmilesField = getOption(OPTION_CANONICAL_SMILES_FIELD, String.class);
        if (canonicalSmilesField == null) {
            throw new IllegalStateException(OPTION_CANONICAL_SMILES_FIELD + " must be specified");
        }
        canonicalSmilesField = canonicalSmilesField.trim();

        List<String> keepFirstFields = readFieldList(OPTION_KEEP_FIRST_FIELDS);
        List<String> keepLastFields = readFieldList(OPTION_KEEP_LAST_FIELDS);
        List<String> appendFields = readFieldList(OPTION_APPEND_FIELDS);


        Stream<MoleculeObject> results = MoleculeObjectUtils.deduplicate(input.getStream(), canonicalSmilesField, keepFirstFields, keepLastFields, appendFields);
        results = addStreamCounter(results, "%s unique smiles from " + input.getSize());
        return new MoleculeObjectDataset(results).getDataset();
    }

    private List<String> readFieldList(String option) {
        String val = getOption(option, String.class);
        List<String> vals = new ArrayList();
        if (val != null) {
            String[] ss = val.split(",");
            for (String s: ss) {
                vals.add(s.trim());
            }
        }
        return vals;
    }

}
