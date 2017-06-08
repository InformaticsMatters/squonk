package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Reads a Tripos Mol2 format file.
 * The structure is passed as an {@link InputStream} (can be gzipped).
 *
 * @author timbo
 */
public class Mol2ReaderStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(Mol2ReaderStep.class.getName());

    /**
     * Expected variable name for the input
     */
    private static final String VAR_FILE_INPUT = StepDefinitionConstants.VARIABLE_FILE_INPUT;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        LOG.info("execute Mol2ReaderStep");
        statusMessage = "Reading file";
        String filename = fetchMappedInput(VAR_FILE_INPUT, String.class, varman);
        statusMessage = "Read Mol2 file " + filename;
    }

}
