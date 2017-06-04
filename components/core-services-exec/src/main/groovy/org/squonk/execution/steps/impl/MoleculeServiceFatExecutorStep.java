package org.squonk.execution.steps.impl;

import org.squonk.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;
import org.squonk.util.StatsRecorder;

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

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;
        String endpoint = getHttpExecutionEndpoint();
        Dataset dataset = fetchMappedInput("input", Dataset.class, varman);

        Map<String, Object> requestHeaders = new HashMap<>();
        Map<String, Object> responseHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        // NOTE: setting the Content-Encoding will cause camel to gzip the data, we don't need to do it
        requestHeaders.put("Content-Encoding", "gzip");
        if (jobId != null) {
            requestHeaders.put(StatsRecorder.HEADER_SQUONK_JOB_ID, jobId);
        }
        statusMessage = "Executing ...";
        try (InputStream input =  dataset.getInputStream(false)) {
            InputStream output = CamelUtils.doRequestUsingHeadersAndQueryParams(context, "POST", endpoint, input, requestHeaders, responseHeaders, options);

            // start debug output
//          String data = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(output), 1000);
//          output.close();
//          LOG.info("Results: |" + data + "|");
//          output = new ByteArrayInputStream(data.getBytes());
            // end debug output

            LOG.fine("Creating Dataset");
            statusMessage = "Handling results ...";
            String responseMetadataJson = (String) responseHeaders.get(CamelCommonConstants.HEADER_METADATA);
            DatasetMetadata responseMetadata = null;
            if (responseMetadataJson == null) {
                responseMetadata = new DatasetMetadata(MoleculeObject.class);
            } else {
                responseMetadata = JsonHandler.getInstance().objectFromJson(responseMetadataJson, DatasetMetadata.class);
            }
            Dataset<MoleculeObject> results = JsonHandler.getInstance().unmarshalDataset(responseMetadata, IOUtils.getGunzippedInputStream(output));
            LOG.fine("Dataset created");
            createMappedOutput("output", Dataset.class, results, varman);
            statusMessage = generateStatusMessage(dataset.getSize(), results.getSize(), -1);
            LOG.info("Results: " + results.getMetadata());
        }
     }

}
