package com.squonk.notebook.execution;

import com.im.lac.job.jobdef.StepDefinition;
import static com.im.lac.job.jobdef.StepDefinitionConstants.*;
import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.execution.steps.DatasetMergerStep;
import java.util.HashMap;
import java.util.Map;

/**
 * Merges datasets
 *
 * Created by timbo on 10/11/15.
 */
public class DatasetMergerQndCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_NAME_DATASET_MERGER = "DatasetMerger";

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {
        // define the step and execution options
        StepDefinition step = new StepDefinition(STEP_DATASET_MERGER)
                .withOption(DatasetMergerStep.OPTION_MERGE_FIELD_NAME, cell.getPropertyMap().get(DatasetMergerStep.OPTION_MERGE_FIELD_NAME))
                .withOption(DatasetMergerStep.OPTION_KEEP_FIRST, cell.getPropertyMap().get(DatasetMergerStep.OPTION_KEEP_FIRST))
                .withFieldMapping(VARIABLE_OUTPUT_DATASET, "results");

        // define the input variable name mappings
        for (int i = 1; i <= 10; i++) {
            String s = (String) cell.getPropertyMap().get(DatasetMergerStep.VAR_INPUT_BASE + i);
            if (s == null) {
                break;
            }
            step = step.withFieldMapping(DatasetMergerStep.VAR_INPUT_BASE + i, DatasetMergerStep.VAR_INPUT_BASE + i);
        }

        return new StepDefinition[]{step};
    }

    @Override
    protected String getCellTypeName() {
        return CELL_TYPE_NAME_DATASET_MERGER;
    }

}
