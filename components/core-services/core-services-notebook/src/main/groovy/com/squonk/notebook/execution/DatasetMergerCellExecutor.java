package com.squonk.notebook.execution;

import com.im.lac.job.jobdef.StepDefinition;
import static com.im.lac.job.jobdef.StepDefinitionConstants.*;
import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.execution.steps.DatasetMergerStep;

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

    public static final String CELL_TYPE_NAME_DATASET_MERGER = "DatasetMergerCellType";
    public static final String INPUT = "Input";

    public DatasetMergerCellExecutor() {
        super(CELL_TYPE_NAME_DATASET_MERGER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {
        // define the step and execution options
        StepDefinition step = new StepDefinition(STEP_DATASET_MERGER)
                .withFieldMapping(VARIABLE_OUTPUT_DATASET, "Results");

        step = configureOption(step, cell, DatasetMergerStep.OPTION_MERGE_FIELD_NAME);
        step = configureOption(step, cell, DatasetMergerStep.OPTION_KEEP_FIRST);

        // define the input variable name mappings
        String s;
        for (int i = 1; i <= 5; i++) {
            s = (String) cell.getPropertyMap().get(INPUT + i);
            if (s == null) {
                break;
            }
            step = step.withFieldMapping(DatasetMergerStep.VAR_INPUT_BASE + i, s);
        }

        return new StepDefinition[]{step};
    }

}
