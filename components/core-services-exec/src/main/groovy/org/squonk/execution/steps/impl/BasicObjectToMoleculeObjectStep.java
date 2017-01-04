package org.squonk.execution.steps.impl;

import org.squonk.types.BasicObject;
import org.squonk.types.TypesUtils;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.MoleculeObject;
import org.squonk.dataset.Dataset;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.CamelContext;

/**
 * Converts a Dataset&lt;BasicObject&gt; to a Dataset&lt;MoleculeObject&gt;
 * given a specified field name that contains the structure. A default name of
 * "structure" is used if option is not specified. The field name is handled in
 * a case insensitive manner.
 *
 * NOTE: if a field with that name is not found MoleculeObjects with an empty
 * structure is generated.
 *
 * NOTE: does not perform any validation of the structure - if bad structures
 * are present errors will occur later when these are parsed.
 *
 * NOTE: This step generates a new Stream that contains the functions necessary
 * to convert the BasicObjects to the MoleculeObjects and creates a new Dataset
 * with that new Stream, but it does NOT call a terminal operation on the Stream
 * to actually do the conversions. You are responsible for providing that
 * terminal operation on the output Dataset.
 *
 * @author timbo
 */
public class BasicObjectToMoleculeObjectStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(BasicObjectToMoleculeObjectStep.class.getName());

    public static final String OPTION_STRUCTURE_FIELD_NAME = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_STRUCTURE_FIELD_NAME;
    public static final String OPTION_STRUCTURE_FORMAT = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_STRUCTURE_FORMAT;
    public static final String OPTION_PRESERVE_UUID = StepDefinitionConstants.ConvertBasicToMoleculeObject.OPTION_PRESERVE_UUID;

    public static String DEFAULT_STRUCTURE_FIELD_NAME = "structure";

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;
        String structureFieldName = getOption(OPTION_STRUCTURE_FIELD_NAME, String.class, DEFAULT_STRUCTURE_FIELD_NAME);
        String structureFormat = getOption(OPTION_STRUCTURE_FORMAT, String.class);
        boolean preserveUuid = getOption(OPTION_PRESERVE_UUID, Boolean.class, true);

        Dataset<BasicObject> input = fetchMappedInput("input", Dataset.class, varman);
        if (input == null) {
            throw new IllegalStateException("No input found");
        } else if (LOG.isLoggable(Level.FINE) && input.getMetadata() != null) {
            LOG.fine("Input has " + input.getMetadata().getSize() + " items");
        }

        statusMessage = "Applying connversions ...";
        Dataset<MoleculeObject> results = TypesUtils.convertBasicObjectDatasetToMoleculeObjectDataset(input, structureFieldName, structureFormat, preserveUuid);

        createMappedOutput("output", Dataset.class, results, varman);
        statusMessage = String.format(MSG_RECORDS_PROCESSED, results.getMetadata().getSize());
        LOG.info("Results: " + results.getMetadata());
    }

}
