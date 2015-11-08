package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.service.adapters.HttpGenericParamsJobAdapter;
import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.util.Map;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class MoleculeServiceFatExecutorStep extends AbstractStep {

    public static final String OPTION_SERVICE_ENDPOINT = "ServiceEndpoint";
    public static final String OPTION_EXECUTION_PARAMS = "ExecutionParams";

    public static final String VAR_INPUT_DATASET = "_MoleculeServiceExecutorInputDataset";
    public static final String VAR_OUTPUT_DATASET = "_MoleculeServiceExecutorOutputDataset";
    
    @Override
    public String[] getInputVariableNames() {
        return new String[]{VAR_INPUT_DATASET};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{VAR_OUTPUT_DATASET};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);
        Dataset input = fetchMappedValue(VAR_INPUT_DATASET, Dataset.class, varman);
        Map<String, Object> params = getOption(OPTION_EXECUTION_PARAMS, Map.class);

        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter();
        InputStream output = adapter.submit(context, endpoint, input.getInputStream(true), params);
        Dataset<MoleculeObject> results = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), output);
        createMappedVariable(VAR_OUTPUT_DATASET, Dataset.class, results, Variable.PersistenceType.DATASET, varman);
     }

}
