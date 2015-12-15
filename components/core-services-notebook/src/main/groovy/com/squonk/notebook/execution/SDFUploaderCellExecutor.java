package com.squonk.notebook.execution;

import com.squonk.execution.steps.StepDefinition;
import static com.squonk.execution.steps.StepDefinitionConstants.*;
import org.squonk.notebook.api.CellDTO;
import com.squonk.execution.steps.impl.SDFReaderStep;

/**
 * Reads a SD file and generates a Dataset of MoleculeObjects containing the 
 * structures and properties defined in the SDF. If a name is defined (the first 
 * line of the CTAB block) this can be handled by setting the NameFieldName option
 * (set to null not to handle the name).
 * The file input is read from the FileInput variable (which must be set prior to 
 * execution) and the results set to the Results variable.
 * Note: the molecules are not validated in any way, and if they are invalid will 
 * result in errors downstream.
 *
 * Created by timbo on 10/11/15.
 */
public class SDFUploaderCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_NAME_SDF_UPLOADER = "SdfUploader";
    
    public SDFUploaderCellExecutor() {
        super(CELL_TYPE_NAME_SDF_UPLOADER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        StepDefinition step = new StepDefinition(STEP_SDF_READER)
                .withFieldMapping(SDFReaderStep.VAR_SDF_INPUT, "FileContent")
                .withFieldMapping(VARIABLE_OUTPUT_DATASET, "Results");

        step = configureOption(step, cell, SDFReaderStep.OPTION_NAME_FIELD_NAME);
        
        return new StepDefinition[]{step};
    }

}
