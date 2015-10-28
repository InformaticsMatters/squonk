package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.service.adapters.HttpGenericParamsJobAdapter;
import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
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
    public static final String FIELD_OUTPUT = "Output";

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);
        InputStream input = fetchMappedValue(FIELD_INPUT, InputStream.class, varman);
        Map<String, Object> params = getOption(OPTION_EXECUTION_PARAMS, Map.class);

        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter();
        InputStream output = adapter.submit(context, endpoint, input, params);
        createMappedVariable(FIELD_OUTPUT, InputStream.class, output, Variable.PersistenceType.NONE, varman);
    }

}
