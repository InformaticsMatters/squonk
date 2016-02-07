package org.squonk.notebook.execution;

import org.squonk.execution.steps.StepDefinition;
import static org.squonk.execution.steps.StepDefinitionConstants.*;
import static org.squonk.execution.steps.StepDefinitionConstants.ChemblActivitiesFetcher.*;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.VariableKey;

/**
 * Given a ChEMBL assay ID fetches all activities for that assay and generated a
 * Dataset or MoleculeObjects containing the structures and activities. Performs
 * this using the REST API provided at the EBI.
 *
 * Created by timbo on 10/11/15.
 */
public class ChemblActivitiesFetcherCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_NAME_CHEMBL_ACTIVITIES_FETCHER = "ChemblActivitiesFetcher";

    public ChemblActivitiesFetcherCellExecutor() {
        super(CELL_TYPE_NAME_CHEMBL_ACTIVITIES_FETCHER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        StepDefinition step = new StepDefinition(CLASSNAME)
                .withOption(OPTION_ASSAY_ID, cell.getOptionMap().get(OPTION_ASSAY_ID).getValue())
                .withOption(OPTION_PREFIX, cell.getOptionMap().get(OPTION_PREFIX).getValue())
                .withOutputVariableMapping(VARIABLE_OUTPUT_DATASET, "results");

        return new StepDefinition[]{step};

    }

}
