package org.squonk.notebook.execution;

import org.squonk.execution.steps.StepDefinition;
import static org.squonk.execution.steps.StepDefinitionConstants.*;
import org.squonk.notebook.api.CellDTO;
import org.squonk.execution.steps.impl.ValueTransformerStep;
import org.squonk.notebook.api.VariableKey;

/**
 * Transform the values of a Dataset. The transforms are defined by the Transformers
 * option which defines a list of individual transforms (extending from 
 * {@link com.squonk.dataset.transform.AbstractTransform}). Current transforms include
 * deleting a value, renaming a value and converting the value to a different type
 * (e.g. String -> Integer). See the {@link com.squonk.dataset.transform} package for 
 * details.
 *
 * Created by timbo on 10/11/15.
 */
public class ValueTransformerCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_NAME_VALUE_TRANSFORMER = "ValueTransformer";
    
    public ValueTransformerCellExecutor() {
        super(CELL_TYPE_NAME_VALUE_TRANSFORMER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        StepDefinition step = new StepDefinition(STEP_VALUE_TRANSFORMER)
                .withInputVariableMapping(VARIABLE_INPUT_DATASET, new VariableKey(cell.getName(), "input"))
                .withOutputVariableMapping(VARIABLE_OUTPUT_DATASET, "results");
        
        // TODO - handle the data type here. The step needs a TransformDefinitions
        // but we might need to gernate that from JSON?
        step = configureOption(step, cell, ValueTransformerStep.OPTION_TRANSFORMS);
        
        return new StepDefinition[]{step};
    }

}
