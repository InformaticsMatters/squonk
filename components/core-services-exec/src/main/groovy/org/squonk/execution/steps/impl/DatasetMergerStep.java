package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import com.im.lac.types.BasicObject;
import org.squonk.dataset.Dataset;

import java.util.List;
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

    public static final String OPTION_MERGE_FIELD_NAME = StepDefinitionConstants.DatasetMerger.OPTION_MERGE_FIELD_NAME;
    public static final String OPTION_KEEP_FIRST = StepDefinitionConstants.DatasetMerger.OPTION_KEEP_FIRST;

    public static final String VAR_INPUT_BASE = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_INPUT_1 = VAR_INPUT_BASE + "1";
    public static final String VAR_INPUT_2 = VAR_INPUT_BASE + "2";
    public static final String VAR_INPUT_3 = VAR_INPUT_BASE + "3";
    public static final String VAR_INPUT_4 = VAR_INPUT_BASE + "4";
    public static final String VAR_INPUT_5 = VAR_INPUT_BASE + "5";

    public static final String VAR_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        String mergeField = getOption(OPTION_MERGE_FIELD_NAME, String.class);
        boolean keepFirst = getOption(OPTION_KEEP_FIRST, Boolean.class, true);
        
        LOG.log(Level.FINE, "Merging using field {0}, keep first value={1}", new Object[]{mergeField, keepFirst});

        Dataset<BasicObject> dataset1 = fetchMappedInput(VAR_INPUT_1, Dataset.class, varman);

        Stream<BasicObject> stream = dataset1.getStream().sequential();
        for (int i = 2; i <= 5; i++) {
            Dataset<BasicObject> nextDataset = fetchMappedInput(VAR_INPUT_BASE + i, Dataset.class, varman);
            if (nextDataset == null) {
                break;
            }
            LOG.log(Level.FINE, "Concatting stream {0}", i);
            stream = Stream.concat(stream, nextDataset.getStream().sequential());
        }

        Map<Object, BasicObject> items = stream.sequential()
                .filter(o -> mergeField == null || o.getValue(mergeField) != null)
                .collect(Collectors.toConcurrentMap(
                                bo -> fetchValueToCompare(bo, mergeField),
                                bo -> bo,
                                (bo1, bo2) -> {
                                    //LOG.info("\nMerging " + bo1 + " and " + bo2 + "\n");
                                    if (keepFirst) {
                                        for (Map.Entry<String, Object> e : bo2.getValues().entrySet()) {
                                            bo1.getValues().putIfAbsent(e.getKey(), e.getValue());
                                        }
                                    } else {
                                        bo1.getValues().putAll(bo2.getValues());
                                    }
                                    return bo1;
                                }));

        Class type = dataset1.getType();
        Dataset result = new Dataset<>(type, items.values());
        createMappedOutput(VAR_OUTPUT, Dataset.class, result, varman);
        LOG.fine("Merge complete. Results: " + result.getMetadata());
    }

    private Object fetchValueToCompare(BasicObject bo, String mergeField) {
        return mergeField == null ? bo.getUUID() : bo.getValue(mergeField);
    }

}
