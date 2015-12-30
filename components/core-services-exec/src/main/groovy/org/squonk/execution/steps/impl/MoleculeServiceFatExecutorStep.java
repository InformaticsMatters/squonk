package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.AbstractStep;
import org.squonk.camel.util.CamelUtils;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.util.Map;

import org.squonk.util.IOUtils;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class MoleculeServiceFatExecutorStep extends AbstractStep {

    public static final String OPTION_SERVICE_ENDPOINT = "ServiceEndpoint";
    public static final String OPTION_EXECUTION_PARAMS = "ExecutionParams";

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);
        Dataset input = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, PersistenceType.DATASET, varman);
        Map<String, Object> params = getOption(OPTION_EXECUTION_PARAMS, Map.class);

        InputStream output = CamelUtils.doPostUsingHeadersAndQueryParams(context, endpoint, input.getInputStream(true), params);
        Dataset<MoleculeObject> results = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), IOUtils.getGunzippedInputStream(output));
        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, results, PersistenceType.DATASET, varman);
     }

}
