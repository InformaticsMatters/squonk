package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import org.squonk.chembl.ChemblClient;
import org.squonk.dataset.Dataset;
import org.apache.camel.CamelContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ChemblActivitiesFetcherStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(ChemblActivitiesFetcherStep.class.getName());

    static final String OPTION_ASSAY_ID = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_ASSAY_ID;
    static final String OPTION_PREFIX = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_PREFIX;
    static final String OPTION_BATCH_SIZE = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_BATCH_SIZE;

    /**
     * The variable for the resulting Dataset&lt;MoleculeObject&gt;
     */
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        dumpConfig(Level.INFO);

        int batchSize = getOption(OPTION_BATCH_SIZE, Integer.class, 100);
        String prefix = getOption(OPTION_PREFIX, String.class);
        String assayID = getOption(OPTION_ASSAY_ID, String.class);
        if (assayID == null) {
            throw new IllegalStateException("Assay ID to fetch not specified");
        }

        ChemblClient client = new ChemblClient();
        Dataset<MoleculeObject> dataset = client.fetchActivitiesForAssay(assayID, batchSize, prefix);

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, dataset, PersistenceType.DATASET, varman);
        LOG.info("ChEMBL fetch complete. Results: " + dataset.getMetadata());
    }

}
