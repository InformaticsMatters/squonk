package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.reader.SDFReader;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Reads a structure and generates a {@link MoleculeObject} of the correspondign format.
 * The structure is passed as an
 * {@link InputStream} (can be gzipped).
 *
 * @author timbo
 */
public class StructureReaderStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(StructureReaderStep.class.getName());

    /**
     * Expected variable name for the input
     */
    private static final String VAR_FILE_INPUT = StepDefinitionConstants.VARIABLE_FILE_INPUT;
    private static final String OPT_FILE_INPUT = StepDefinitionConstants.StructureUpload.OPTION_FILE_FORMAT;
    /**
     * Variable name for the MoleculeObjectDataset output
     */
    private static final String VAR_DATASET_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        LOG.info("execute StructureReaderStep");
        statusMessage = "Reading structure";
        String format = (String)options.get(OPT_FILE_INPUT);
        if (format == null || format.isEmpty()) {
            throw new IllegalStateException("File format must be specified using option " + OPT_FILE_INPUT);
        }
        String filename = fetchMappedInput(VAR_FILE_INPUT, String.class, varman);

        if ("automatic".equalsIgnoreCase(format)) {
            format = guessFileFormat(filename);
        }

        try (InputStream is = fetchMappedInput(VAR_FILE_INPUT, InputStream.class, varman)) {
            LOG.fine("Fetched input for: " + filename);

            boolean gzipped = filename.toLowerCase().endsWith(".gz");
            String mol = IOUtils.convertStreamToString(gzipped ? IOUtils.getGunzippedInputStream(is) : is);
            MoleculeObject mo = new MoleculeObject(mol, format);
            DatasetMetadata meta = new DatasetMetadata(MoleculeObject.class);
            meta.setSize(1);
            Dataset results = new Dataset(MoleculeObject.class, Collections.singletonList(mo), meta);
            LOG.fine("Writing output");
            createMappedOutput(VAR_DATASET_OUTPUT, Dataset.class, results, varman);
            statusMessage = "Structure read as " + format;
        }
    }

    private String guessFileFormat(String filename) {
        if (filename.toLowerCase().endsWith("mol") || filename.toLowerCase().endsWith("mol.gz")) {
            return "mol";
        } else if (filename.toLowerCase().endsWith("pdb") || filename.toLowerCase().endsWith("pdb.gz")) {
            return "pdb";
        } else {
            throw new IllegalStateException("Cannot determine file format. Expected mol or pdb");
        }
    }

}
