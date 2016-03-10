package org.squonk.execution.steps.impl;

import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.util.CamelUtils;
import com.im.lac.types.MoleculeObject;
import org.squonk.core.CommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.DatasetUtils;
import org.squonk.dataset.IncompatibleMetadataException;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;
import org.apache.camel.CamelContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**Thin executor sends only the molecules (no values) to the service and combines the returned values with the
 * originals. As such the network traffic is minimised and the remote end does not need to handle values which it may
 * not be able to represent.
 * Whether the resulting structure is the input structure or the one returned by the service is determined by the
 * OPTION_PRESERVE_STRUCTURE option.
 *
 * NOTE: the input is held in memory until the corresponding molecule is returned from the service which usually means that
 * the large datasets will be handled OK, but in some cases there could be issues. Examples include when results are returned
 * out of order or when only a subset of the input molecules are returned.
 *
 *
 * @author timbo
 */
public class MoleculeServiceThinExecutorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(MoleculeServiceThinExecutorStep.class.getName());

    public static final String OPTION_SERVICE_ENDPOINT = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_ENDPOINT;
    public static final String OPTION_EXECUTION_PARAMS = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_PARAMS;
    public static final String OPTION_PRESERVE_STRUCTURE = StepDefinitionConstants.ServiceExecutor.OPTION_PRESERVE_STRUCTURE;

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        Dataset<MoleculeObject> dataset = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, PersistenceType.DATASET, varman);
        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);
        Map<String, Object> params = getOption(OPTION_EXECUTION_PARAMS, Map.class);
        Boolean preserveStructure = getOption(OPTION_PRESERVE_STRUCTURE, Boolean.class, true);
        DatasetMetadata<MoleculeObject> requestMetadata = dataset.getMetadata();

        //LOG.info("Initial metadata: " + metadata);

        Map<UUID, MoleculeObject> cache = new ConcurrentHashMap<>();
        Stream<MoleculeObject> stream = dataset.getStream()
                .map(fat -> {
                    cache.put(fat.getUUID(), fat);
                    //LOG.info("Fat molecule:  " + fat);
                    MoleculeObject thin = new MoleculeObject(fat.getUUID(), fat.getSource(), fat.getFormat());
                    //LOG.info("Thin molecule: " + thin);
                    return thin;
                });

        InputStream input = JsonHandler.getInstance().marshalStreamToJsonArray(stream, true);

        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        
        // send for execution
        Map<String, Object> responseHeaders = new HashMap<>();
        InputStream output = CamelUtils.doRequestUsingHeadersAndQueryParams(context, "POST", endpoint, input, requestHeaders, responseHeaders, params);


//        String data = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(output), 1000);
//        //LOG.info("Data: " + data);
//        output = new ByteArrayInputStream(data.getBytes());

        // merge the metadata
        String responseMetadataJson = (String)responseHeaders.get(CamelCommonConstants.HEADER_METADATA);
        DatasetMetadata responseMetadata = null;
        if (responseMetadataJson == null) {
            responseMetadata = new DatasetMetadata(MoleculeObject.class);
        } else {
            responseMetadata = JsonHandler.getInstance().objectFromJson(responseMetadataJson,DatasetMetadata.class);
        }
        DatasetMetadata mergedMetadata = null;
        if (requestMetadata == null && responseMetadata == null) {
            mergedMetadata = new DatasetMetadata(MoleculeObject.class);
        } else if (requestMetadata != null && responseMetadata != null) {
            try {
                mergedMetadata = DatasetUtils.mergeDatasetMetadata(requestMetadata, responseMetadata);
            } catch (IncompatibleMetadataException ex) {
                mergedMetadata = new DatasetMetadata(MoleculeObject.class);
            }
        } else if  (requestMetadata != null) {
            mergedMetadata = requestMetadata;
        } else if  (responseMetadata != null) {
            mergedMetadata = responseMetadata;
        }


        Dataset<MoleculeObject> outputMols = JsonHandler.getInstance().unmarshalDataset(mergedMetadata, IOUtils.getGunzippedInputStream(output));
        if (outputMols.getMetadata() != null) {
            LOG.info("Response metadata: " + outputMols.getMetadata());
        }

        // merge the results back into the original data
        Stream<MoleculeObject> resultMols = outputMols.getStream().map(m -> {
            //LOG.info("Handling: " + m);
            UUID uuid = m.getUUID();
            MoleculeObject o = cache.get(uuid);
            if (o == null) {
                LOG.warning("Molecule " + uuid + " not found. Strange!");
                return m;
            } else {
                LOG.fine("Found Mol " + uuid);
                MoleculeObject neu;
                if (preserveStructure) {
                    o.getValues().putAll(m.getValues());
                    neu = o;
                } else {
                    neu = new MoleculeObject(uuid, m.getSource(), m.getFormat());
                    neu.getValues().putAll(o.getValues());
                    neu.getValues().putAll(m.getValues());
                }
                cache.remove(uuid);
                return neu;
            }
        });

        Dataset<MoleculeObject> results = new Dataset<>(MoleculeObject.class, resultMols, mergedMetadata);

        createMappedOutput(VAR_OUTPUT_DATASET, Dataset.class, results, PersistenceType.DATASET, varman);
    }

}
