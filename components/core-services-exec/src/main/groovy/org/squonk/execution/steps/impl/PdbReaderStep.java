package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.MoleculeObject;
import org.squonk.types.PDBFile;
import org.squonk.util.IOUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Reads a PDB format file.
 * The structure is passed as an {@link InputStream} (can be gzipped).
 *
 * @author timbo
 */
public class PdbReaderStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(PdbReaderStep.class.getName());

    /**
     * Expected variable name for the input
     */
    private static final String VAR_FILE_INPUT = StepDefinitionConstants.VARIABLE_FILE_INPUT;
//    private static final String OPT_FILE_INPUT = StepDefinitionConstants.StructureUpload.OPTION_FILE_FORMAT;
    /**
     * Variable name for output
     */
    private static final String VAR_PDB_OUTPUT = StepDefinitionConstants.PdbUpload.VARIABLE_PDB;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        LOG.info("execute PdbReaderStep");
        statusMessage = "Reading file";
//        String format = (String)options.get(OPT_FILE_INPUT);
//        if (format == null || format.isEmpty()) {
//            throw new IllegalStateException("File format must be specified using option " + OPT_FILE_INPUT);
//        }
        String filename = fetchMappedInput(VAR_FILE_INPUT, String.class, varman);

//        try (InputStream is = fetchMappedInput(VAR_FILE_INPUT, InputStream.class, varman)) {
//            LOG.fine("Fetched input for: " + filename);
//
//            PDBFile pdb = new PDBFile(IOUtils.getGunzippedInputStream(is));
//            LOG.fine("Writing pdb output");
//            createMappedOutput(VAR_PDB_OUTPUT, PDBFile.class, pdb, varman);
//            statusMessage = "Read PDB file " + filename;
//        }
        statusMessage = "Read PDB file " + filename;
    }



}
