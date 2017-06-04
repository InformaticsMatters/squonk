package org.squonk.execution.steps.impl;

import org.squonk.core.HttpServiceDescriptor;
import org.squonk.dataset.*;
import org.squonk.io.IODescriptor;
import org.squonk.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.util.CamelUtils;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.StatsRecorder;

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

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {


        statusMessage = MSG_PREPARING_INPUT;

        IODescriptor inputDescriptor = getSingleInputDescriptor();
        ThinDescriptor td = getThinDescriptor(inputDescriptor);
        ThinDatasetWrapper thinWrapper = null;
        if (td != null) {
            thinWrapper = DatasetUtils.createThinDatasetWrapper(td, inputDescriptor.getSecondaryType(), options);
        }

        Dataset<MoleculeObject> sourceDataset = fetchMappedInput("input", Dataset.class, varman);
        Dataset<MoleculeObject> dataset;
        if (thinWrapper != null) {
            dataset = thinWrapper.prepareInput(sourceDataset);
        } else {
            dataset = sourceDataset;
        }

        String endpoint = getHttpExecutionEndpoint();

        InputStream input = JsonHandler.getInstance().marshalStreamToJsonArray(dataset.getStream(), false);
//            String inputData = IOUtils.convertStreamToString(input);
//            LOG.info("Input: " + inputData);
//            input = new ByteArrayInputStream(inputData.getBytes());

        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        // NOTE: setting the Content-Encoding will cause camel to gzip the data, we don't need to do it
        requestHeaders.put("Content-Encoding", "gzip");
        if (jobId != null) {
            requestHeaders.put(StatsRecorder.HEADER_SQUONK_JOB_ID, jobId);
        }

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

        Dataset<MoleculeObject> results = new Dataset<>(output, metadata);

        createMappedOutput("output", Dataset.class, results, varman);
        statusMessage = generateStatusMessage(sourceDataset.getSize(), results.getSize(), -1);
        LOG.info("Results: " + results.getMetadata());
    }

}
