package org.squonk.notebook.execution;

import org.squonk.execution.steps.StepDefinition;
import static org.squonk.execution.steps.StepDefinitionConstants.*;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.BindingDTO;
import org.squonk.notebook.api.CellDTO;
import org.squonk.execution.steps.impl.DatasetMergerStep;
import org.squonk.notebook.api.OptionDTO;
import org.squonk.notebook.api.VariableKey;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Merges one or more datasets by removing duplicates and merging the data. The
 * inputs are provided by variables named Input1, Input2 ... up to Input5. The
 * order is important and must be sequential starting from 1. The following
 * options are handled.
 * <ol>
 * <li>MergeFieldName (String): The name of the value (field) whose value is use
 * to identify the duplicate records. If not specified then the BasicObject's
 * UUUID is used.</li>
 * <li>KeepFirst (Boolean, default true): in the case of a duplicate whether to
 * keep values from the original objects (true) or to replace with those from
 * the new one (false)</li>
 * </ol>
 *
 * Created by timbo on 10/11/15.
 */
public class DatasetMergerCellExecutor extends AbstractStepExecutor {

    private static final Logger LOG = Logger.getLogger(DatasetMergerCellExecutor.class.getName());

    public static final String CELL_TYPE_NAME_DATASET_MERGER = "DatasetMerger";
    public static final String INPUT = "input";

    public DatasetMergerCellExecutor() {
        super(CELL_TYPE_NAME_DATASET_MERGER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        dumpCellConfig(cell, Level.INFO);

        // define the step and execution options
        StepDefinition step = new StepDefinition(DatasetMerger.CLASSNAME)
                .withOutputVariableMapping(VARIABLE_OUTPUT_DATASET, "results");

        step = configureOption(step, cell, DatasetMergerStep.OPTION_MERGE_FIELD_NAME);
        step = configureOption(step, cell, DatasetMergerStep.OPTION_KEEP_FIRST);

        // define the input variable name mappings
        BindingDTO o;
        for (int i = 1; i <= 5; i++) {
            o = (BindingDTO) cell.getBindingMap().get(INPUT + i);
            if (o == null || o.getVariableKey() == null) {
                LOG.info("Variable " + (INPUT + i) + " not bound.");
                break;
            }
            LOG.info("Setting " + DatasetMergerStep.VAR_INPUT_BASE + i + " to " + o.getVariableKey().getName() + " from " + o.getVariableKey().getProducerName());
            step = step.withInputVariableMapping(DatasetMergerStep.VAR_INPUT_BASE + i, o.getVariableKey());
        }

        return new StepDefinition[]{step};
    }

}
