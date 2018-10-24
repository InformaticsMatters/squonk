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
import org.squonk.execution.steps.AbstractThinDatasetStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Step that enriches one dataset with data from another. Information form the second is merged into the first.
 * The data that is merged can be the core information (e.g. the source and format for a Dataset of MoleculeObjects)
 * or the values or both. The datasets do not need to be of the same type, but if they are different only the values can be
 * merged.
 *
 * The records can be matched based on a field name (the OPT_MERGE_FIELD option) or if not specified by the UUID.
 * Records in the second dataset not present in the first (e.g.. identified by the field name or UUID) are ignored.
 * Where data is common between both records (e.g. both contain values for a specific field) the data in the first
 * dataset is replaced with that from the second.
 *
 * The result is all the data in the first dataset "overlaid" with any data from matching records in the second.
 *
 * Two options control what is merged. OPT_MERGE_MAIN specifies to merge the main content (e.g. structure info for a
 * MoleculeObject), (if datasets are are different types as determined by {@link Dataset#getType()} then this option
 * is ignored) whilst OPT_MERGE_VALUES specified to merge the values. Both are boolean values and one or both must
 * be specified.
 *
 * The second dataset is collected in memory, so very large datasets should be avoided. There should be no reasonable
 * limit for the first dataset.
 *
 * This step is somewhat similar to the {@link DatasetMergerStep}
 *
 * @author timbo
 */
public class DatasetEnricherStep extends AbstractThinDatasetStep {

    private static final Logger LOG = Logger.getLogger(DatasetEnricherStep.class.getName());

    public static final String OPT_MAIN_FIELD = StepDefinitionConstants.DatasetEnricher.OPTION_MAIN_FIELD;
    public static final String OPT_EXTRA_FIELD = StepDefinitionConstants.DatasetEnricher.OPTION_EXTRA_FIELD;
    public static final String OPT_MERGE_MODE = StepDefinitionConstants.DatasetEnricher.OPTION_MERGE_MODE;

    public static final String VAR_INPUT = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_NEW_DATA = StepDefinitionConstants.DatasetEnricher.VARIABLE_NEW_DATA;


    public static final String VAR_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    private static final String SOURCE = "Squonk DatasetEnricherStep";

    private enum Mode {main, values, both};

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.enricher.v1", "DatasetEnricher", "Enrich a dataset with content from another dataset",
            new String[]{"enrich", "merge", "dataset"},
            null, "icons/merge.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            new IODescriptor[]{
                    IODescriptors.createBasicObjectDataset(StepDefinitionConstants.DatasetEnricher.VARIABLE_NEW_DATA),
                    IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET)
            },
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{

                    new OptionDescriptor<>(new DatasetFieldTypeDescriptor(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new Class[]{String.class, Integer.class}),
                            StepDefinitionConstants.DatasetEnricher.OPTION_MAIN_FIELD, "Input field name",
                            "Name of field in input which identifies equivalent entries", OptionDescriptor.Mode.User)
                            .withMinMaxValues(0, 1),

                    new OptionDescriptor<>(new DatasetFieldTypeDescriptor(StepDefinitionConstants.DatasetEnricher.VARIABLE_NEW_DATA, new Class[]{String.class, Integer.class}),
                            StepDefinitionConstants.DatasetEnricher.OPTION_EXTRA_FIELD, "NewData field name",
                            "Name of field in newData which identifies equivalent entries", OptionDescriptor.Mode.User)
                            .withMinMaxValues(0, 1),

                    new OptionDescriptor<>(String.class, StepDefinitionConstants.DatasetEnricher.OPTION_MERGE_MODE, "Mode: main, values or both",
                            "Merge the main content (e.g the structure for molecules), the values or both", OptionDescriptor.Mode.User)
                            .withValues(new String[]{"main", "values", "both"})
                            .withDefaultValue("both")
                            .withMinMaxValues(1, 1)

            },
            null, null, null,
            DatasetEnricherStep.class.getName()
    );

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        final String mainField = getOption(OPT_MAIN_FIELD, String.class);
        final String extraField = getOption(OPT_EXTRA_FIELD, String.class);
        final String mergeMode = getOption(OPT_MERGE_MODE, String.class, "both");
        final Mode mode = Mode.valueOf(mergeMode);

        Dataset<? extends BasicObject> mainDataset = fetchMappedInput(VAR_INPUT, Dataset.class, varman);
        Dataset<? extends BasicObject> extraDataset = fetchMappedInput(VAR_NEW_DATA, Dataset.class, varman);
        if (mainDataset == null) {
            throw new IllegalStateException("Input dataset not defined");
        }
        if (extraDataset == null) {
            throw new IllegalStateException("Dataset with new data not defined");
        }
        Class mainType = mainDataset.getType();
        Class extraType = extraDataset.getType();
        if (mode != Mode.values && mainType != extraType) {
            throw new IllegalStateException("Can't merge main content if dataset types are different");
        }
        DatasetMetadata mainMeta = mainDataset.getMetadata();
        DatasetMetadata extraMeta = extraDataset.getMetadata();


        statusMessage = "Merging data";

        Map<Object, BasicObject> newData = new HashMap<>();
        Set<String> updatedFields = Collections.synchronizedSet(new HashSet<>());
        try (Stream<? extends BasicObject> st = extraDataset.getStream()) {
            st.forEachOrdered((BasicObject bo) -> {
                Object comparator = fetchValueToCompare(bo, extraField);
                if (comparator != null) {
                    BasicObject existing = newData.get(comparator);
                    if (existing != null) {
                        throw new IllegalStateException("Values to merge contain duplicate keys: " + comparator);
                    } else {
                        bo.clearValue(extraField);
                        newData.put(comparator, bo);
                        if (mode != Mode.main) {
                            updatedFields.addAll(bo.getValues().keySet());
                        }
                    }
                }
            });
        }

        AtomicInteger count = new AtomicInteger(0);
        Stream<? extends BasicObject> enrichedStream = mainDataset.getStream().peek((BasicObject bo) -> {
            Object comparator = fetchValueToCompare(bo, mainField);
            if (comparator != null) {
                BasicObject updates = newData.get(comparator);
                if (updates != null) {
                    if (mode != Mode.values) {
                        bo.merge(updates, mode == Mode.main);
                    } else {
                        bo.getValues().putAll(updates.getValues());
                    }
                    count.incrementAndGet();
                }
            }
        });

        Object extraDesc = extraMeta.getProperties().get(DatasetMetadata.PROP_DESCRIPTION);
        String what = null;
        if (mode == Mode.both) {
            what = "Main content and values";
        } else if (mode == Mode.main) {
            what = "Main content";
        } else if (mode == Mode.values) {
            what = "Values";
        }
        mainMeta.appendDatasetHistory(what + " enriched with data from " +
                (extraDesc == null ? "[no description]" : extraDesc.toString()));

        List<DatasetMetadata.PropertiesHolder> propHolders =  mainMeta.getFieldMetaProps();
        for (DatasetMetadata.PropertiesHolder propHolder : propHolders) {
            String fld = propHolder.getFieldName();
            if (updatedFields.contains(fld)) {
                updatedFields.remove(fld);
                mainMeta.appendFieldHistory(fld, "Field enriched with additional data");
            }
        }
        Map<String,Class> mappings = extraMeta.getValueClassMappings();
        for (String name : updatedFields) {
            Class type = mappings.get(name);
            mainMeta.createField(name, SOURCE, "Created from enriched data", type);
        }

        statusMessage = "Writing results";
        Dataset output = new Dataset(mainType, enrichedStream, mainMeta);
        createMappedOutput(VAR_OUTPUT, Dataset.class, output, varman);
        statusMessage = count.get() + " records updated";
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(output.getMetadata()));
    }

    private Object fetchValueToCompare(BasicObject bo, String mergeField) {
        return mergeField == null ? bo.getUUID() : bo.getValue(mergeField);
    }

    @Override
    public Map<String, Object> executeForVariables(Map<String, Object> inputs, CamelContext context) throws Exception {
        // TODO - implement this
        throw new RuntimeException("NYI");
    }

}
