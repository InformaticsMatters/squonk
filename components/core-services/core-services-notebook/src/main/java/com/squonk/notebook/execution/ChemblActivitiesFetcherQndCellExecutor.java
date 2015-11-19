package com.squonk.notebook.execution;

import com.squonk.notebook.client.CallbackClient;
import com.im.lac.job.jobdef.StepDefinition;
import com.im.lac.job.jobdef.StepDefinitionConstants;
import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.api.CellType;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

/**
 * Given a ChEMBL assay ID fetches all activities for that assay and generated a Dataset or
 * MoleculeObjects containing the structures and activites.
 * Performs this using the REST API provided at the EBI.
 *
 * Created by timbo on 10/11/15.
 */
public class ChemblActivitiesFetcherQndCellExecutor implements QndCellExecutor {
    @Inject
    private CallbackClient callbackClient;


    @Override
    public boolean handles(CellType cellType) {
        return "ChemblActivitiesFetcher".equals(cellType.getName());
    }

    @Override
    public void execute(String cellName) {
        CellDTO cell = callbackClient.retrieveCell(cellName);
        String assayID = (String) cell.getPropertyMap().get("assayId");
        String prefix = (String) cell.getPropertyMap().get("prefix");
        // real implmentation class not yet accessible so using the inner class as a mock for now
        ChemblClient client = new ChemblClient();
        // the batchSize of 100 should be thought of as an advanced option - not present in the standard
        // UI but able to be specified using "Advanced" settings. For now we hard code a sensible value.
        Dataset<MoleculeObject> dataset = client.fetchActivitiesForAssay(assayID, 100, prefix);

        try {
            // As it´s a DATASET variable type we write metatada to value and contents as any stream-based variable(like FILE)
            Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
            try (Stream stream = generator.getAsStream()) {
                InputStream dataInputStream = generator.getAsInputStream(stream, true);
                callbackClient.writeStreamContents(cellName, "results", dataInputStream);
            }
            DatasetMetadata metadata = generator.getDatasetMetadata();
            callbackClient.writeTextValue(cellName, "results", JsonHandler.getInstance().objectToJson(metadata));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void executeUsingSteps(String cellName) {
        CellDTO cell = callbackClient.retrieveCell(cellName);

        // define the execution options
        Map<String,Object> options = new HashMap<>();
        options.put("AssayID", cell.getPropertyMap().get("assayId"));
        options.put("Prefix", cell.getPropertyMap().get("prefix"));

        // define the variable name mappings
        Map<String,String> mappings = new HashMap<>();
        mappings.put("_OutputDataset", "results");

        // define the step(s)
        StepDefinition step = new StepDefinition(StepDefinitionConstants.STEP_CHEMBL_ACTIVITIES_FETCHER,
                options,mappings);


        // execute, passing in the cell name/id which is needed to get/set the variables
        //cellExecutionService.execute(cell.getName(), step);

        // we really want to avoid needing a custom exectuor for every cell type and we could avoid
        // this if the cell implementation generates the StepDefinition(s). The cell itself knows how
        // its UI components need to be bound to its exection so can easily generate this.
        // If this was so then all cells that are implemented using steps can have a single generic executor.

    }

    /**
     * This is a mock for the real class
     */
    class ChemblClient {
        Dataset<MoleculeObject> fetchActivitiesForAssay(String assayID, int batchSize, String prefix) {
            List<MoleculeObject> mols = new ArrayList<>();
            mols.add(new MoleculeObject("C", "smiles", Collections.singletonMap(prefix, 1.1)));
            mols.add(new MoleculeObject("CC", "smiles", Collections.singletonMap(prefix, 2.2)));
            mols.add(new MoleculeObject("CCC", "smiles", Collections.singletonMap(prefix, 3.3)));
            return new Dataset<>(MoleculeObject.class, mols);
        }
    }
}
