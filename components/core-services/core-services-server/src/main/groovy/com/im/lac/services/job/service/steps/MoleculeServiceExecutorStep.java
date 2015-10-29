package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.service.adapters.HttpGenericParamsJobAdapter;
import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.DatasetMetadata;
import java.io.InputStream;
import java.util.Map;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class MoleculeServiceExecutorStep extends AbstractStep {

    public static final String OPTION_SERVICE_ENDPOINT = "ServiceEndpoint";
    public static final String OPTION_EXECUTION_PARAMS = "ExecutionParams";

    public static final String FIELD_INPUT = "Input";
    public static final String FIELD_OUTPUT_DATA = "OutputData";
    public static final String FIELD_OUTPUT_METADATA = "OutputMetadata";
    
    @Override
    public String[] getInputVariableNames() {
        return new String[]{FIELD_INPUT};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{FIELD_OUTPUT_DATA, FIELD_OUTPUT_METADATA};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);
        InputStream input = fetchMappedValue(FIELD_INPUT, InputStream.class, varman);
        Map<String, Object> params = getOption(OPTION_EXECUTION_PARAMS, Map.class);

        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter();
        InputStream output = adapter.submit(context, endpoint, input, params);
        createMappedVariable(FIELD_OUTPUT_DATA, InputStream.class, output, Variable.PersistenceType.NONE, varman);
        createMappedVariable(FIELD_OUTPUT_METADATA, DatasetMetadata.class, new DatasetMetadata(MoleculeObject.class), Variable.PersistenceType.NONE, varman);
    }

}
