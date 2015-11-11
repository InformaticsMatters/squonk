package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import com.squonk.chembl.ChemblClient;
import com.squonk.dataset.Dataset;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class ChemblActivitiesFetcherStep extends AbstractStep {

    public static final String OPTION_ASSAY_ID = "AssayID";
    public static final String OPTION_PREFIX = "Prefix";
    public static final String OPTION_BATCH_SIZE = "BatchSize";

    /**
     * The variable for the resulting Dataset&lt;MoleculeObject&gt;
     */
    public static final String VAR_OUTPUT_DATASET = "_ChemblActivitiesFetcherOutputDataset";

    @Override
    public String[] getInputVariableNames() {
        return new String[]{};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{VAR_OUTPUT_DATASET};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        int batchSize = getOption(OPTION_BATCH_SIZE, Integer.class, 100);
        String prefix = getOption(OPTION_PREFIX, String.class);
        String assayID = getOption(OPTION_ASSAY_ID, String.class);
        if (assayID == null) {
            throw new IllegalStateException("Assay ID to fetch not specified");
        }

        ChemblClient client = new ChemblClient();
        Dataset<MoleculeObject> dataset = client.fetchActivitiesForAssay(assayID, batchSize, prefix);

        createMappedVariable(VAR_OUTPUT_DATASET, Dataset.class, dataset, Variable.PersistenceType.DATASET, varman);
    }

}
