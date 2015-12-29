package org.squonk.notebook.execution;

import org.squonk.execution.steps.StepDefinition;

import static org.squonk.execution.steps.StepDefinitionConstants.*;

import org.squonk.notebook.api.BindingDTO;
import org.squonk.notebook.api.CellDTO;
import org.squonk.execution.steps.impl.BasicObjectToMoleculeObjectStep;

import java.util.logging.Level;

/**
 * Converts a Dataset of BasicObjects to a Dataset of MoleculeObjects.
 * Note: the molecules are not validated in any way, and if they are invalid will
 * result in errors downstream.
 * <p>
 * Inputs: Dataset<BasicObject> named _InputDataset ({@link org.squonk.execution.steps.StepDefinitionConstants#VARIABLE_INPUT_DATASET})
 * <p>
 * Outputs: Dataset<MoleculeObject> named _OutputDataset ({@link org.squonk.execution.steps.StepDefinitionConstants#VARIABLE_OUTPUT_DATASET})
 * <p>
 * Options:
 * <br>
 * StructureFieldName: for the name of the field (value) to use for the structure.
 * <br>
 * StructureFormat: for the format of the structures (smiles or mol)
 * <br>
 * PreserveUuid: whether to preserve the UUID of the input objects, or to generate new ones
 * <p>
 * Created by timbo on 10/11/15.
 */
public class BasicObjectToMoleculeObjectCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_BASICOBJECT_TO_MOLECULEOBJECT = "BasicObjectToMoleculeObject";

    public BasicObjectToMoleculeObjectCellExecutor() {
        super(CELL_TYPE_BASICOBJECT_TO_MOLECULEOBJECT);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        dumpCellConfig(cell, Level.INFO);

        BindingDTO input = cell.getBindingMap().get("input");
        if (input == null) {
            throw new IllegalStateException("No input variable named 'input' found");
        }

        StepDefinition step = new StepDefinition(STEP_BASICOBJECT_TO_MOLECULEOBJECT)
                .withInputVariableMapping(VARIABLE_INPUT_DATASET, input.getVariableKey())
                .withOutputVariableMapping(VARIABLE_OUTPUT_DATASET, "output");

        step = configureOption(step, cell, BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME, true);
        step = configureOption(step, cell, BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FORMAT, false);
        step = configureOption(step, cell, BasicObjectToMoleculeObjectStep.OPTION_PRESERVE_UUID, false);

        return new StepDefinition[]{step};

    }

}
