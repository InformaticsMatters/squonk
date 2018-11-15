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

import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractThinStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetsFieldOptionDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;
import org.squonk.dataset.Dataset;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.camel.CamelContext;
import org.squonk.types.io.JsonHandler;

/**
 * Merges multiple datasets that must be of the same type. Designed for combining multiple datasets into a single
 * dataset. New records are appended to the resulting dataset. Existing records have their data merged. How it is merged
 * is specified by the options.
 * Up to 5 datasets can be merged (we hope to make this more flexible in future).
 * <p>
 * This step is somewhat similar to the {@link DatasetEnricherStep}
 *
 * @author timbo
 */
public class DatasetMergerStep extends AbstractThinStep {

    private static final Logger LOG = Logger.getLogger(DatasetMergerStep.class.getName());

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.merger.v1", "DatasetMerger", "Merge datasets into one",
            new String[]{"merge", "dataset"},
            null, "icons/merge.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            new IODescriptor[]{
                    IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 1),
                    IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 2),
                    IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 3),
                    IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 4),
                    IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 5)
            },
            // TODO - find a way to dynamically determine the output type based on the first bound input
            // the consequence of not doing this is that the merger cell always reports its output as BasicObject
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{

                    new DatasetsFieldOptionDescriptor(StepDefinitionConstants.DatasetMerger.OPTION_MERGE_FIELD_NAME,
                            "Merge field name", "Name of value field which identifies equivalent entries"),

                    new OptionDescriptor<>(Boolean.class, StepDefinitionConstants.DatasetMerger.OPTION_KEEP_FIRST, "When duplicate keep first",
                            "When duplicate field name use the existing value rather than the new one", OptionDescriptor.Mode.User)
                            .withDefaultValue(true)
            },
            null, null, null,
            DatasetMergerStep.class.getName()
    );

    public static final String OPTION_MERGE_FIELD_NAME = StepDefinitionConstants.DatasetMerger.OPTION_MERGE_FIELD_NAME;
    public static final String OPTION_KEEP_FIRST = StepDefinitionConstants.DatasetMerger.OPTION_KEEP_FIRST;

    public static final String VAR_INPUT_BASE = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_INPUT_1 = VAR_INPUT_BASE + "1";
    public static final String VAR_INPUT_2 = VAR_INPUT_BASE + "2";
    public static final String VAR_INPUT_3 = VAR_INPUT_BASE + "3";
    public static final String VAR_INPUT_4 = VAR_INPUT_BASE + "4";
    public static final String VAR_INPUT_5 = VAR_INPUT_BASE + "5";

    public static final String VAR_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    private static final String SOURCE = "Squonk DatasetMergerStep";


    private Object fetchValueToCompare(BasicObject bo, String mergeField) {
        return mergeField == null ? bo.getUUID() : bo.getValue(mergeField);
    }

    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs, CamelContext context) throws Exception {

        if (inputs == null || inputs.size() == 0) {
            throw new IllegalArgumentException("No data to merge");
        }

        String mergeField = getOption(OPTION_MERGE_FIELD_NAME, String.class);
        boolean keepFirst = getOption(OPTION_KEEP_FIRST, Boolean.class, true);

        LOG.log(Level.INFO, "Merging using field {0}, keep first value={1}", new Object[]{mergeField, keepFirst});
        statusMessage = "Merging using field " + mergeField;

        Map<Object, BasicObject> results = new LinkedHashMap<>();
        DatasetMetadata<? extends BasicObject> meta = null;

        Class type = null;
        int count = 0;
        int totalRecordCount = 0;
        List<String> sources = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Dataset<? extends BasicObject> nextDataset = (Dataset)inputs.get(VAR_INPUT_BASE + i);
            if (nextDataset == null) {
                LOG.info("Input" + i + " not present");
                continue;
            } else {
                LOG.info("Processing input" + i);
                count++;
                int size = nextDataset.getSize();
                if (size < 0) {
                    totalRecordCount = -1;
                } else if (totalRecordCount >= 0) {
                    totalRecordCount += size;
                }
                DatasetMetadata<? extends BasicObject> oldMeta = nextDataset.getMetadata();
                Object source = oldMeta.getProperties().get(DatasetMetadata.PROP_SOURCE);
                sources.add(source == null ? "undefined source" : source.toString());
                if (type == null) {
                    type = nextDataset.getType();
                    meta = new DatasetMetadata(type);
                    meta.getProperties().put(DatasetMetadata.PROP_CREATED, DatasetMetadata.now());
                    meta.getProperties().put(DatasetMetadata.PROP_SOURCE, SOURCE);
                }

                for (DatasetMetadata.PropertiesHolder ph : oldMeta.getFieldMetaProps()) {
                    String fldName = ph.getFieldName();
                    if (meta.getFieldMetaProp(fldName, DatasetMetadata.PROP_CREATED) == null) {
                        if (fldName.equals(mergeField)) {
                            meta.createField(fldName, SOURCE, "Common key used for merging", null);
                            meta.appendFieldHistory(fldName, "Values defined from merge key");
                        } else {
                            meta.createField(fldName, SOURCE, "Values merged from multiple datasets", null);
                            meta.appendFieldHistory(fldName, "Values merged from multiple datasets");
                        }
                    }
                }

                statusMessage = "Handling dataset " + i;
                LOG.log(Level.INFO, "Handling dataset {0}", i);
                Stream<? extends BasicObject> st = nextDataset.getStream();
                st.forEachOrdered((BasicObject bo) -> {
                    Object comparator = fetchValueToCompare(bo, mergeField);
                    BasicObject existing = results.get(comparator);
                    if (existing != null) {
                        if (keepFirst) {
                            for (Map.Entry<String, Object> e : bo.getValues().entrySet()) {
                                existing.getValues().putIfAbsent(e.getKey(), e.getValue());
                            }
                        } else {
                            existing.getValues().putAll(bo.getValues());
                        }
                    } else {
                        results.put(comparator, bo);
                    }
                });
                st.close();

            }
        }

        meta.getProperties().put(DatasetMetadata.PROP_DESCRIPTION, "Merged from " + count
                + " datasets using field " + mergeField
                + ". Sources were: " + sources.stream().collect(Collectors.joining(", ")));

        Dataset output = new Dataset(results.values(), meta);
        statusMessage = generateStatusMessage(totalRecordCount, output.getSize(), -1);
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(output.getMetadata()));
        return Collections.singletonMap(VAR_OUTPUT, output);
    }

}
