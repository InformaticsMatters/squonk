/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractThinDatasetStep;
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
import java.util.logging.Logger;
import java.util.stream.Stream;

/**Thin executor sends only the molecules (no values) to the service and gets back an unrelated set of BasicObjects which
 * become the results
 *
 * TODO REMOVE THIS CLASS
 *
 *
 * @author timbo
 */
public class MoleculeServiceToBasicObjectThinExecutorStep extends AbstractThinDatasetStep {

    private static final Logger LOG = Logger.getLogger(MoleculeServiceToBasicObjectThinExecutorStep.class.getName());

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;

        Dataset<MoleculeObject> dataset = fetchMappedInput("input", Dataset.class, varman);
        String endpoint = getHttpExecutionEndpoint();

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

        Dataset<BasicObject> results = new Dataset<>(output, metadata);

        createMappedOutput("output", Dataset.class, results, varman);
        statusMessage = generateStatusMessage(dataset.getSize(), results.getSize(), -1);
        LOG.info("Results: " + results.getMetadata());
    }

    @Override
    public Map<String, Object> executeWithData(Map<String, Object> inputs, CamelContext context) throws Exception {
        return null;
    }

}
