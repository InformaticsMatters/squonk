package org.squonk.execution.steps.impl;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.DatasetUtils;
import org.squonk.dataset.IncompatibleMetadataException;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**Thin executor either has no body or gets its body from the option whose key is 'body'
 * This is POSTed to the service and Stream<MoleculeObject> is returned
 *
 *
 * @author timbo
 */
public class OutOnlyMoleculeServiceExecutorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(OutOnlyMoleculeServiceExecutorStep.class.getName());

    public static final String OPTION_SERVICE_ENDPOINT = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_ENDPOINT;
    public static final String OPTION_EXECUTION_PARAMS = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_PARAMS;

    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        MoleculeObject bodyOption = getOption("body", MoleculeObject.class);

        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);
        Map<String, Object> params = getOption(OPTION_EXECUTION_PARAMS, Map.class);

        String input = null;
        if (bodyOption != null) {
            JsonHandler.getInstance().objectToJson(bodyOption);
            LOG.info("Input: " + input);
        }

        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        
        // send for execution
        Map<String, Object> responseHeaders = new HashMap<>();

        InputStream output = CamelUtils.doRequestUsingHeadersAndQueryParams(context, "POST", endpoint, input == null ? null : new ByteArrayInputStream(input.getBytes()), requestHeaders, responseHeaders, params);

//        String data = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(output), 1000);
//        LOG.info("Results: " + data);
//        output = new ByteArrayInputStream(data.getBytes());

        // read the response metadata, if any
        String responseMetadataJson = (String)responseHeaders.get(CamelCommonConstants.HEADER_METADATA);
        DatasetMetadata<MoleculeObject> responseMetadata = null;
        if (responseMetadataJson == null) {
            responseMetadata = new DatasetMetadata(MoleculeObject.class);
        } else {
            responseMetadata = JsonHandler.getInstance().objectFromJson(responseMetadataJson, DatasetMetadata.class);
        }


        Dataset<MoleculeObject> results = JsonHandler.getInstance().unmarshalDataset(responseMetadata, IOUtils.getGunzippedInputStream(output));
        if (results.getMetadata() != null) {
            LOG.info("Response metadata: " + results.getMetadata());
        }

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, results, varman);
    }

}
