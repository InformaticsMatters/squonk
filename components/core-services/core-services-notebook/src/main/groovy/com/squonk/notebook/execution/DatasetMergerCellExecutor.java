package com.squonk.notebook.execution;

import com.im.lac.job.jobdef.StepDefinition;
import static com.im.lac.job.jobdef.StepDefinitionConstants.*;
import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.execution.steps.DatasetMergerStep;

/**
 * Merges datasets
 *
 * Created by timbo on 10/11/15.
 */
public class DatasetMergerCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_NAME_DATASET_MERGER = "DatasetMergerCellType";

    public DatasetMergerCellExecutor() {
        super(CELL_TYPE_NAME_DATASET_MERGER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {
        // define the step and execution options
        StepDefinition step = new StepDefinition(STEP_DATASET_MERGER)
                .withFieldMapping(VARIABLE_OUTPUT_DATASET, "results");

        step = configureOption(step, cell, DatasetMergerStep.OPTION_MERGE_FIELD_NAME);
        step = configureOption(step, cell, DatasetMergerStep.OPTION_KEEP_FIRST);

        // define the input variable name mappings
        for (int i = 1; i <= 5; i++) {
            String s = (String) cell.getPropertyMap().get(DatasetMergerStep.VAR_INPUT_BASE + i);
            if (s == null) {
                break;
            }
            step = step.withFieldMapping(DatasetMergerStep.VAR_INPUT_BASE + i, DatasetMergerStep.VAR_INPUT_BASE + i);
        }

        return new StepDefinition[]{step};
    }

}
