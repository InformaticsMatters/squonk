package org.squonk.execution.steps.impl;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class MoleculeServiceFatExecutorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(MoleculeServiceFatExecutorStep.class.getName());

    public static final String OPTION_SERVICE_ENDPOINT = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_ENDPOINT;
    public static final String OPTION_EXECUTION_PARAMS = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_PARAMS;

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);
        Dataset input = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, PersistenceType.DATASET, varman);
        Map<String, Object> params = getOption(OPTION_EXECUTION_PARAMS, Map.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip");
        LOG.info("POSTing to service");
        InputStream output = CamelUtils.doRequestUsingHeadersAndQueryParams(context, "POST",  endpoint, input.getInputStream(true), headers, null, params);
        LOG.fine("Creating Dataset");
        Dataset<MoleculeObject> results = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), IOUtils.getGunzippedInputStream(output));
        LOG.fine("Dataset created");
        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, results, PersistenceType.DATASET, varman);
        LOG.info("Dataset written to variable");
     }

}
