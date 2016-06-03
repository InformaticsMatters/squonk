package org.squonk.execution.steps.impl;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Reads one set of molecules and returns an unrelated set of molecules
 *
 * @author timbo
 */
public class MoleculeServiceBasicExecutorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(MoleculeServiceBasicExecutorStep.class.getName());

    public static final String OPTION_SERVICE_ENDPOINT = StepDefinitionConstants.OPTION_SERVICE_ENDPOINT;

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;

        Dataset<MoleculeObject> dataset = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman);
        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);

        InputStream input = JsonHandler.getInstance().marshalStreamToJsonArray(dataset.getStream(), false);
//            String inputData = IOUtils.convertStreamToString(input);
//            LOG.info("Input: " + inputData);
//            input = new ByteArrayInputStream(inputData.getBytes());

        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        // NOTE: setting the Content-Encoding will cause camel to gzip the data, we don't need to do it
        requestHeaders.put("Content-Encoding", "gzip");

        // send for execution
        statusMessage = "Posting request ...";
        Map<String, Object> responseHeaders = new HashMap<>();
        InputStream output = CamelUtils.doRequestUsingHeadersAndQueryParams(context, "POST", endpoint, input, requestHeaders, responseHeaders, options);
        statusMessage = "Handling results ...";

//        String data = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(output), 1000);
//        LOG.info("Results: " + data);
//        output = new ByteArrayInputStream(data.getBytes());

        // fetch the metadata
        String metadataJson = (String) responseHeaders.get(CamelCommonConstants.HEADER_METADATA);
        DatasetMetadata metadata;
        if (metadataJson == null || "null".equals(metadataJson)) {
            metadata = new DatasetMetadata(MoleculeObject.class);
        } else {
            metadata = JsonHandler.getInstance().objectFromJson(metadataJson, DatasetMetadata.class);
        }

        Dataset<MoleculeObject> results = new Dataset<>(MoleculeObject.class, output, metadata);

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, results, varman);
        statusMessage = String.format(MSG_RECORDS_PROCESSED, results.getMetadata().getSize());
        LOG.info("Results: " + results.getMetadata());
    }

}
