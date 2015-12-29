package org.squonk.notebook.execution;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.impl.TrustedGroovyDatasetScriptStep;
import org.squonk.notebook.api.BindingDTO;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.OptionDTO;

import java.util.logging.Level;

import static org.squonk.execution.steps.StepDefinitionConstants.*;

/**
 * Use a client defined Groovy script to process an input Dataset and generate and a output dataset.
 * The script is trusted in the sense that it has been vetted to contain no malicious code and can be executed directly
 * in the Java process. Untrusted scripts will be executed within a Docker sandbox.
 * <p>
 * Created by timbo on 10/11/15.
 */
public class TrustedGroovyDatasetScriptCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_NAME_TRUSTED_GROOVY_DATASET_SCRIPT = "TrustedGroovyDatasetScript";

    public TrustedGroovyDatasetScriptCellExecutor() {
        super(CELL_TYPE_NAME_TRUSTED_GROOVY_DATASET_SCRIPT);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        dumpCellConfig(cell, Level.INFO);

        BindingDTO input = cell.getBindingMap().get("input");
        if (input == null) {
            throw new IllegalStateException("No input variable named 'input' found");
        }

        StepDefinition step = new StepDefinition(STEP_TRUSTED_GROOVY_DATASET_SCRIPT)
                .withInputVariableMapping(VARIABLE_INPUT_DATASET, input.getVariableKey())
                .withOutputVariableMapping(VARIABLE_OUTPUT_DATASET, "output");

        OptionDTO dto = cell.getOptionMap().get(TrustedGroovyDatasetScriptStep.OPTION_SCRIPT);
        if (dto == null || dto.getValue() == null) {
            throw new IllegalStateException("No script found");
        }
        step.withOption(TrustedGroovyDatasetScriptStep.OPTION_SCRIPT, dto.getValue());

        return new StepDefinition[]{step};
    }

}
