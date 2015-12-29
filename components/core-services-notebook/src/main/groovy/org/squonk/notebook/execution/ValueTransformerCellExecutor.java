package org.squonk.notebook.execution;

import com.squonk.dataset.transform.TransformDefinitions;
import com.squonk.util.GroovyScriptExecutor;
import org.squonk.execution.steps.StepDefinition;
import static org.squonk.execution.steps.StepDefinitionConstants.*;

import org.squonk.notebook.api.BindingDTO;
import org.squonk.notebook.api.CellDTO;
import org.squonk.execution.steps.impl.ValueTransformerStep;
import org.squonk.notebook.api.OptionDTO;

import javax.script.*;
import java.util.logging.Level;

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

    public static final String CELL_TYPE_NAME_VALUE_TRANSFORMER = "TransformValues";
    
    public ValueTransformerCellExecutor() {
        super(CELL_TYPE_NAME_VALUE_TRANSFORMER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        dumpCellConfig(cell, Level.INFO);

        BindingDTO input = cell.getBindingMap().get("input");
        if (input == null) {
            throw new IllegalStateException("No input variable named 'input' found");
        }

        StepDefinition step = new StepDefinition(STEP_VALUE_TRANSFORMER)
                .withInputVariableMapping(VARIABLE_INPUT_DATASET, input.getVariableKey())
                .withOutputVariableMapping(VARIABLE_OUTPUT_DATASET, "output");
        
        // TODO - handle the data type here. The step needs a TransformDefinitions instance
        // for now we assume we get a groovy script that creates the transform definitions
        OptionDTO dto = cell.getOptionMap().get(ValueTransformerStep.OPTION_TRANSFORMS);
        if (dto ==null || dto.getValue() == null) {
            throw new IllegalStateException("No transform definitions found");
        }

        try {
            ScriptEngine engine = GroovyScriptExecutor.createScriptEngine(this.getClass().getClassLoader());
            String script = GroovyScriptExecutor.addImportsToScript((String)dto.getValue(), new String[] {"com.squonk.dataset.transform.TransformDefinitions"});
            TransformDefinitions txs = GroovyScriptExecutor.executeAndReturnValue(TransformDefinitions.class, engine, script, null);
            step.withOption(ValueTransformerStep.OPTION_TRANSFORMS, txs);

            return new StepDefinition[]{step};

        } catch (ScriptException e) {
            throw new RuntimeException("Failed to execute", e);
        }

    }

}
