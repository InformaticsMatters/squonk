package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.im.lac.types.BasicObject;
import com.squonk.dataset.Dataset;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class DatasetMergerStep extends AbstractStep {
    
    private static final Logger LOG = Logger.getLogger(DatasetMergerStep.class.getName());

    /**
     * The name of the value to use to merge the entries. If undefined then the
     * UUID is used which is probably not what you want. Expects a String value.
     */
    public static final String OPTION_MERGE_FIELD_NAME = "MergeFieldName";
    /**
     * In the case of duplicate field names whether to keep the original value
     * (true) or to replace this with newly found value (false). Expects a
     * Boolean value. Default is true.
     */
    public static final String OPTION_KEEP_FIRST = "KeepFirst";

    private static final String FIELD_INPUT_BASE = "_SimpleDatasetMergerInput";
    public static final String FIELD_INPUT_1 = FIELD_INPUT_BASE + "1";
    public static final String FIELD_INPUT_2 = FIELD_INPUT_BASE + "2";
    public static final String FIELD_INPUT_3 = FIELD_INPUT_BASE + "3";
    public static final String FIELD_INPUT_4 = FIELD_INPUT_BASE + "4";
    public static final String FIELD_INPUT_5 = FIELD_INPUT_BASE + "5";

    public static final String FIELD_OUTPUT = "_SimpleDatasetMergerOutput";

    @Override
    public String[] getInputVariableNames() {
        return new String[]{FIELD_INPUT_1, FIELD_INPUT_2, FIELD_INPUT_3, FIELD_INPUT_4, FIELD_INPUT_5};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{FIELD_OUTPUT};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        String mergeField = getOption(OPTION_MERGE_FIELD_NAME, String.class);
        boolean keepFirst = getOption(OPTION_KEEP_FIRST, Boolean.class, true);
        
        LOG.log(Level.INFO, "Merging using field {0}, keep first value = {1}", new Object[]{mergeField, keepFirst});

        Dataset<BasicObject> dataset1 = fetchMappedValue(FIELD_INPUT_1, Dataset.class, varman);

        Stream<BasicObject> stream = dataset1.getStream();
        for (int i = 2; i <= 5; i++) {
            Dataset<BasicObject> nextDataset = fetchMappedValue(FIELD_INPUT_BASE + i, Dataset.class, varman);
            if (nextDataset == null) {
                break;
            }
            LOG.log(Level.FINE, "Concatting stream {0}", i);
            stream = Stream.concat(stream, nextDataset.getStream());
        }

        Map<Object, BasicObject> items = stream
                .filter(o -> mergeField == null || o.getValue(mergeField) != null)
                .collect(Collectors.toConcurrentMap(
                                bo -> fetchValueToCompare(bo, mergeField),
                                bo -> bo,
                                (bo1, b02) -> {
                                    if (keepFirst) {
                                        for (Map.Entry<String, Object> e : b02.getValues().entrySet()) {
                                            bo1.getValues().putIfAbsent(e.getKey(), e.getValue());
                                        }
                                    } else {
                                        bo1.getValues().putAll(b02.getValues());
                                    }
                                    return bo1;
                                }));

        Class type = dataset1.getType();
        Dataset result = new Dataset(type, items.values());

        createMappedVariable(FIELD_OUTPUT, Dataset.class, result, Variable.PersistenceType.DATASET, varman);
    }

    private Object fetchValueToCompare(BasicObject bo, String mergeField) {
        return mergeField == null ? bo.getUUID() : bo.getValue(mergeField);
    }

}
