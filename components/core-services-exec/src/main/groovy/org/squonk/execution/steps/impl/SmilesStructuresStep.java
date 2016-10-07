package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by timbo on 13/09/16.
 */
public class SmilesStructuresStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(SmilesStructuresStep.class.getName());

    protected static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    protected static final String OPTION_SMILES = StepDefinitionConstants.SmilesStructures.OPTION_SMILES;
    protected static final String FIELD_NAME = "Name";

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;


        String text = getOption(OPTION_SMILES, String.class);
        if (text == null) {
            throw new IllegalStateException("Smiles structures must be defined as option named " + OPTION_SMILES);
        }

        String[] lines = text.split("\n");
        List<MoleculeObject> mols = new ArrayList<>();
        boolean hasName = false;
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 0) {
                String[] parts = line.split("\\s+", 2);
                if (parts.length > 0) {
                    MoleculeObject mo = new MoleculeObject(parts[0], "smiles");
                    if (parts.length == 2) {
                        hasName = true;
                        mo.putValue(FIELD_NAME, parts[1]);
                    }
                    mols.add(mo);
                }
            }
        }
        DatasetMetadata<MoleculeObject> meta = new DatasetMetadata<>(MoleculeObject.class);
        meta.getProperties().put(DatasetMetadata.PROP_CREATED, DatasetMetadata.now());
        meta.getProperties().put(DatasetMetadata.PROP_SOURCE, "User provided Smiles");
        meta.getProperties().put(DatasetMetadata.PROP_DESCRIPTION, "Read from user provided Smiles");
        if (hasName) {
            meta.createField(FIELD_NAME, "User provided name", "Name provided by user with smiles", String.class);
        }
        meta.setSize(mols.size());


        Dataset<MoleculeObject> result = new Dataset<>(MoleculeObject.class, mols, meta);

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, result, varman);

        statusMessage = String.format(MSG_RECORDS_PROCESSED, result.getMetadata().getSize());
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(result.getMetadata()));
    }


}
