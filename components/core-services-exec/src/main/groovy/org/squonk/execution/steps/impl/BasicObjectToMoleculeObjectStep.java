package org.squonk.execution.steps.impl;

import com.im.lac.types.TypesUtils;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetProvider;

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

    /** The name of the value in the BasicObject that contains the structure */
    public static final String OPTION_STRUCTURE_FIELD_NAME = "structureFieldName";
    /** Optional value for the structure format. e.g. "smiles". */
    public static final String OPTION_STRUCTURE_FORMAT = "structureFormat";
    /** Option for whether to use the UUID of BasicObject as the UUID of the new MolecuelObject.
     * Default is true. Expects a boolean value.
     */  
    public static final String OPTION_PRESERVE_UUID = "preserveUuid";

    /** The variable for the source Dataset&lt;BasicObject&gt; */
    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    /** The variable for the resulting Dataset&lt;MoleculeObject&gt; */
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    public static String DEFAULT_STRUCTURE_FIELD_NAME = "structure";

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        String structureFieldName = getOption(OPTION_STRUCTURE_FIELD_NAME, String.class, DEFAULT_STRUCTURE_FIELD_NAME);
        String structureFormat = getOption(OPTION_STRUCTURE_FORMAT, String.class);
        boolean preserveUuid = getOption(OPTION_PRESERVE_UUID, Boolean.class, true);

        DatasetProvider p = fetchMappedInput(VAR_INPUT_DATASET, DatasetProvider.class, PersistenceType.DATASET, varman);
        Dataset input = p.getDataset();
        if (LOG.isLoggable(Level.FINE) && input.getMetadata() != null) {
            LOG.fine("Input has " + input.getMetadata().getSize() + " items");
        }

        Dataset<MoleculeObject> output = TypesUtils.convertBasicObjectDatasetToMoleculeObjectDataset(input, structureFieldName, structureFormat, preserveUuid);

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, output, PersistenceType.DATASET, varman);
    }

}
