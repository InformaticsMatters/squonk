package com.squonk.notebook.execution;

import com.im.lac.job.jobdef.StepDefinition;
import static com.im.lac.job.jobdef.StepDefinitionConstants.*;
import com.squonk.notebook.api.CellDTO;

/**
 * Given a ChEMBL assay ID fetches all activities for that assay and generated a
 * Dataset or MoleculeObjects containing the structures and activities. Performs
 * this using the REST API provided at the EBI.
 *
 * Created by timbo on 10/11/15.
 */
public class ChemblActivitiesFetcherCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_NAME_CHEMBL_ACTIVITIES_FETCHER = "ChemblActivitiesFetcherCellType";

    public ChemblActivitiesFetcherCellExecutor() {
        super(CELL_TYPE_NAME_CHEMBL_ACTIVITIES_FETCHER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        StepDefinition step = new StepDefinition(STEP_CHEMBL_ACTIVITIES_FETCHER)
                .withOption("AssayID", cell.getPropertyMap().get("assayId"))
                .withOption("Prefix", cell.getPropertyMap().get("prefix"))
                .withFieldMapping(VARIABLE_OUTPUT_DATASET, "results");

        return new StepDefinition[]{step};

    }

}
