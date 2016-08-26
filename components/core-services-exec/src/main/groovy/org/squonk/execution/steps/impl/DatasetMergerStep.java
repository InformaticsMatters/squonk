package org.squonk.execution.steps.impl;

import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
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
 *
 * @author timbo
 */
public class DatasetMergerStep extends AbstractStep {
    
    private static final Logger LOG = Logger.getLogger(DatasetMergerStep.class.getName());

    public static final String OPTION_MERGE_FIELD_NAME = StepDefinitionConstants.DatasetMerger.OPTION_MERGE_FIELD_NAME;
    public static final String OPTION_KEEP_FIRST = StepDefinitionConstants.DatasetMerger.OPTION_KEEP_FIRST;

    public static final String VAR_INPUT_BASE = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_INPUT_1 = VAR_INPUT_BASE + "1";
    public static final String VAR_INPUT_2 = VAR_INPUT_BASE + "2";
    public static final String VAR_INPUT_3 = VAR_INPUT_BASE + "3";
    public static final String VAR_INPUT_4 = VAR_INPUT_BASE + "4";
    public static final String VAR_INPUT_5 = VAR_INPUT_BASE + "5";

    public static final String VAR_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    private static final String SOURCE = "Squonk Dataset merger";

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        String mergeField = getOption(OPTION_MERGE_FIELD_NAME, String.class);
        boolean keepFirst = getOption(OPTION_KEEP_FIRST, Boolean.class, true);
        
        LOG.log(Level.INFO, "Merging using field {0}, keep first value={1}", new Object[]{mergeField, keepFirst});

        Map<Object, BasicObject> results = new LinkedHashMap<>();
        DatasetMetadata<? extends BasicObject> meta = null;

        Class type = null;
        int count = 0;
        List<String> sources = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Dataset<? extends BasicObject> nextDataset = fetchMappedInput(VAR_INPUT_BASE + i, Dataset.class, varman);

            if (nextDataset != null) {
                count++;
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

        meta.getProperties().put(DatasetMetadata.PROP_DESCRIPTION, "Merged from " + count + " datasets using field " + mergeField
        + ". Sources were: " + sources.stream().collect(Collectors.joining(", ")));

        if (type != null) {
            Dataset output = new Dataset(type, results.values(), meta);
            createMappedOutput(VAR_OUTPUT, Dataset.class, output, varman);
            statusMessage = String.format(MSG_RECORDS_PROCESSED, output.getMetadata().getSize());
            LOG.info("Results: " + JsonHandler.getInstance().objectToJson(output.getMetadata()));
        } else {
            LOG.info("No data to merge");
        }
    }

    private Object fetchValueToCompare(BasicObject bo, String mergeField) {
        return mergeField == null ? bo.getUUID() : bo.getValue(mergeField);
    }

}
