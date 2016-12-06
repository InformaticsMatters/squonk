package org.squonk.execution.steps.impl;

import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.reader.SDFReader;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.CamelContext;

/**
 * Reads a SDFile and generates a {@link Dataset} of
 * {@link MoleculeObject}s. The SDFile is passed as an
 * {@link java.io.InputStream} (can be gzipped). By default the
 input is expected in the variable named by the VAR_SDF_INPUT
 constant, though that name can be mapped to a different name. The resulting
 Dataset is contained in the variable named by the VAR_DATASET_OUTPUT constant.
 *
 * @author timbo
 */
public class SDFReaderStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(SDFReaderStep.class.getName());

    /**
     * How to handle the name field (the first line of the CTAB block). See
     * {@link SDFReader} for details.
     */
    public static final String OPTION_NAME_FIELD_NAME = StepDefinitionConstants.SdfUpload.OPTION_NAME_FIELD_NAME;
    /**
     * Expected variable name for the input
     */
    public static final String VAR_SDF_INPUT = StepDefinitionConstants.VARIABLE_FILE_INPUT;
    /**
     * Variable name for the MoleculeObjectDataset output
     */
    public static final String VAR_DATASET_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        LOG.info("execute SDFReaderStep");
        statusMessage = "Reading SDF";
        String filename = fetchMappedInput(VAR_SDF_INPUT, String.class, varman);
        try (InputStream is = fetchMappedInput(VAR_SDF_INPUT, InputStream.class, varman)) {
            LOG.fine("Fetched input for: " + filename);
            SDFReader reader = createReader(IOUtils.getGunzippedInputStream(is), filename);
            LOG.fine("Created SDFReader");
            try (Stream<MoleculeObject> mols = reader.asStream()) {
                DatasetMetadata meta = reader.getDatasetMetadata();
                Dataset results = new Dataset(MoleculeObject.class, mols, meta);
                LOG.fine("Writing output");
                createMappedOutput(VAR_DATASET_OUTPUT, Dataset.class, results, varman);
                statusMessage = String.format(MSG_RECORDS_PROCESSED, results.getMetadata().getSize());
                LOG.fine("Writing dataset from SDF complete: " + JsonHandler.getInstance().objectToJson(results.getMetadata()));
            }
        }
    }

    private SDFReader createReader(InputStream input, String filename) throws IOException {
        SDFReader reader = new SDFReader(input, filename);
        String nameFieldName = getOption(OPTION_NAME_FIELD_NAME, String.class);
        if (nameFieldName != null && nameFieldName.length() > 0) {
            reader.setNameFieldName(nameFieldName);
        } else {
            reader.setNameFieldName(null);
        }
        return reader;
    }

}
