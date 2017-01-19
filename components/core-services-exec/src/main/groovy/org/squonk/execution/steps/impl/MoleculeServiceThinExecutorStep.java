package org.squonk.execution.steps.impl;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.camel.CamelContext;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;
import org.squonk.util.StatsRecorder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** Thin executor sends only the molecules (no values) to the service and combines the returned values with the
 * originals. As such the network traffic is minimised and the remote end does not need to handle values which it may
 * not be able to represent.
 * Whether the resulting structure is the input structure or the one returned by the service is determined by the
 * OPTION_PRESERVE_STRUCTURE option.
 *
 * NOTE: the input is held in memory until the corresponding molecule is returned from the service which usually means that
 * the large datasets will be handled OK, but in some cases there could be issues. Examples include when results are returned
 * out of order or when only a subset of the input molecules are returned.
 *
 * TODO - make this support generic fully so that it can handle things other than MoleculeObjects
 *
 *
 * @author timbo
 */
public class MoleculeServiceThinExecutorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(MoleculeServiceThinExecutorStep.class.getName());

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        updateStatus(MSG_PREPARING_INPUT);

        Dataset<MoleculeObject> inputDataset = fetchMappedInput("input", Dataset.class, varman);
        String endpoint = getHttpExecutionEndpoint();
        boolean preserveStructure = getOption(StepDefinitionConstants.MoleculeServiceThinExecutor.OPTION_PRESERVE_STRUCTURE, Boolean.class, true);
        boolean filter = getOption(StepDefinitionConstants.MoleculeServiceThinExecutor.OPTION_FILTER, Boolean.class, false);
        LOG.info("Filter mode: " + filter);

        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        // NOTE: setting the Content-Encoding will cause camel to gzip the data, we don't need to do it
        requestHeaders.put("Content-Encoding", "gzip");
        if (jobId != null) {
            requestHeaders.put(StatsRecorder.HEADER_SQUONK_JOB_ID, jobId);
        }

        ThinDatasetWrapper thinWrapper = new ThinDatasetWrapper(inputDataset.getType(), filter, preserveStructure);
        Dataset<MoleculeObject> thinDataset = thinWrapper.prepareInput(inputDataset);


        InputStream inputStream = JsonHandler.getInstance().marshalStreamToJsonArray(thinDataset.getStream(), false);

        // send for execution
        updateStatus("Posting request ...");
        Map<String, Object> responseHeaders = new HashMap<>();
        InputStream outputStream = CamelUtils.doRequestUsingHeadersAndQueryParams(context, "POST", endpoint, inputStream, requestHeaders, responseHeaders, options);
        updateStatus("Handling results ...");

        // start debug output
//        String data = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(output), 1000);
//        LOG.info("Results: |" + data + "|");
//        output = new ByteArrayInputStream(data.getBytes());
        // end debug output

        String responseMetadataJson = (String)responseHeaders.get(CamelCommonConstants.HEADER_METADATA);
        DatasetMetadata<? extends BasicObject> responseMetadata;
        if (responseMetadataJson == null) {
            responseMetadata = new DatasetMetadata<>(MoleculeObject.class);
            LOG.info("No Metadata returned from service. Assuming MoleculeObjects");
        } else {
            LOG.info("Using response metadata: " + responseMetadataJson);
            responseMetadata = JsonHandler.getInstance().objectFromJson(responseMetadataJson, DatasetMetadata.class);
        }


        Dataset<? extends BasicObject> responseResults;
        try {
            responseResults = JsonHandler.getInstance().unmarshalDataset(responseMetadata, IOUtils.getGunzippedInputStream(outputStream));
        } catch (JsonParseException jpe) {
            throw new RuntimeException("Service returned invalid JSON");
        }

        Dataset<MoleculeObject> resultDataset = thinWrapper.generateOutput(responseResults);

        createMappedOutput("output", Dataset.class, resultDataset, varman);
        updateStatus(String.format(MSG_RECORDS_PROCESSED, resultDataset.getMetadata().getSize()));
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(resultDataset.getMetadata()));
    }

}
