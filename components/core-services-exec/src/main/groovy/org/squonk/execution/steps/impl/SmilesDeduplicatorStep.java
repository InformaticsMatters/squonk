package org.squonk.execution.steps.impl;

import org.squonk.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.util.MoleculeObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class SmilesDeduplicatorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(SmilesDeduplicatorStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_CANONICAL_SMILES_FIELD = StepDefinitionConstants.SmilesDeduplicator.OPTION_CANONICAL_SMILES_FIELD;
    public static final String OPTION_KEEP_FIRST_FIELDS = StepDefinitionConstants.SmilesDeduplicator.OPTION_KEEP_FIRST_FIELDS;
    public static final String OPTION_KEEP_LAST_FIELDS = StepDefinitionConstants.SmilesDeduplicator.OPTION_KEEP_LAST_FIELDS;
    public static final String OPTION_APPEND_FIELDS = StepDefinitionConstants.SmilesDeduplicator.OPTION_APPEND_FIELDS;

    /**
     * Add the transforms to the dataset Stream. NOTE: transforms will not occur
     * until a terminal operation is performed on the Stream. Normally no output is
     * created as the transforms are added to the input dataset which will be
     * transient, however if an output field is needed then specify a mapping for the 
     * field named FIELD_OUTPUT_DATASET. 
     *
     * @param varman
     * @param context
     * @throws Exception
     */
    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;
        Dataset ds = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman);
        if (ds == null) {
            throw new IllegalStateException("Input variable not found: " + VAR_INPUT_DATASET);
        }
        LOG.info("Input Dataset: " + ds);

        String canonicalSmilesField = getOption(OPTION_CANONICAL_SMILES_FIELD, String.class);
        if (canonicalSmilesField == null) {
            throw new IllegalStateException(OPTION_CANONICAL_SMILES_FIELD + " must be specified");
        }
        canonicalSmilesField = canonicalSmilesField.trim();

        List<String> keepFirstFields = readFieldList(OPTION_KEEP_FIRST_FIELDS);
        List<String> keepLastFields = readFieldList(OPTION_KEEP_LAST_FIELDS);
        List<String> appendFields = readFieldList(OPTION_APPEND_FIELDS);


        Stream<MoleculeObject> results = MoleculeObjectUtils.deduplicate(ds.getStream(), canonicalSmilesField, keepFirstFields, keepLastFields, appendFields);

        LOG.info("Processing complete");
        
        String outFldName = mapOutputVariable(VAR_OUTPUT_DATASET);
        if (outFldName != null) {
            Dataset<MoleculeObject> output = new MoleculeObjectDataset(results).getDataset();
            createVariable(outFldName, Dataset.class, output, varman);
            statusMessage = generateStatusMessage(ds.getSize(), output.getSize(), -1);
            LOG.info("Results: " + output.getMetadata());
        }

    }

    private List<String> readFieldList(String option) {
        String val = getOption(option, String.class);
        List<String> vals = new ArrayList();
        if (val != null) {
            String[] ss = val.split(",");
            for (String s: ss) {
                vals.add(s.trim());
            }
        }
        return vals;
    }

}
