package org.squonk.execution.steps.impl;

import com.fasterxml.jackson.core.JsonParseException;
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
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;
import org.squonk.util.StatsRecorder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**Thin executor sends only the molecules (no values) to the service and gets back an unrelated set of BasicObjects which
 * become the results
 *
 *
 * @author timbo
 */
public class MoleculeServiceToBasicObjectThinExecutorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(MoleculeServiceToBasicObjectThinExecutorStep.class.getName());

    public static final String OPTION_SERVICE_ENDPOINT = StepDefinitionConstants.OPTION_SERVICE_ENDPOINT;

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;

        Dataset<MoleculeObject> dataset = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman);
        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);

        DatasetMetadata<MoleculeObject> requestMetadata = dataset.getMetadata();

        LOG.info("Initial metadata: " + requestMetadata);

        Stream<MoleculeObject> thinStream = dataset.getStream().sequential()
                .map(fat -> {
                    //LOG.info("Fat molecule:  " + fat);
                    MoleculeObject thin = new MoleculeObject(fat.getUUID(), fat.getSource(), fat.getFormat());
                    //LOG.info("Thin molecule: " + thin);
                    return thin;
                });

        InputStream input = JsonHandler.getInstance().marshalStreamToJsonArray(thinStream, false);
        // some remotes don't seem to support data being streamed so we must materialize it
        boolean streamSupport = getOption("option.streamsupport", Boolean.class, true);
        if (!streamSupport) {
            String inputData = IOUtils.convertStreamToString(input);
            LOG.info("Materialized input of length: " + inputData.length());
            input = new ByteArrayInputStream(inputData.getBytes());
        }
        // end materializing

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

        // fetch the metadata
        String metadataJson = (String) responseHeaders.get(CamelCommonConstants.HEADER_METADATA);
        DatasetMetadata metadata;
        if (metadataJson == null || "null".equals(metadataJson)) {
            metadata = new DatasetMetadata(BasicObject.class);
        } else {
            metadata = JsonHandler.getInstance().objectFromJson(metadataJson, DatasetMetadata.class);
        }

        Dataset<BasicObject> results = new Dataset<>(BasicObject.class, output, metadata);

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, results, varman);
        statusMessage = String.format(MSG_RECORDS_PROCESSED, results.getMetadata().getSize());
        LOG.info("Results: " + results.getMetadata());
    }

}
