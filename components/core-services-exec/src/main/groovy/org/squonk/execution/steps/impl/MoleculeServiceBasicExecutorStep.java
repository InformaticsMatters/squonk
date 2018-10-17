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
import org.squonk.dataset.*;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
import org.squonk.types.MoleculeObject;
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
public class MoleculeServiceBasicExecutorStep extends AbstractDatasetStep {

    private static final Logger LOG = Logger.getLogger(MoleculeServiceBasicExecutorStep.class.getName());

    @Override
    public void execute(VariableManager varman, CamelContext camelContext) throws Exception {

        statusMessage = MSG_FETCHING_INPUT;

        Dataset<MoleculeObject> input = fetchMappedInput("input", Dataset.class, varman);

        Dataset<MoleculeObject> results = doExecuteWithDataset(input, camelContext);

        createMappedOutput("output", Dataset.class, results, varman);
        statusMessage = generateStatusMessage(input.getSize(), results.getSize(), -1);
        LOG.info("Results: " + results.getMetadata());
    }

    @Override
    protected Dataset doExecuteWithDataset(Dataset input, CamelContext camelContext) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;

        // TODO - it may be simple to handle the thin processing of the input using AbstractThinDatasetStep

        IODescriptor inputDescriptor = getSingleInputDescriptor();
        ThinDescriptor td = getThinDescriptor(inputDescriptor);
        ThinDatasetWrapper thinWrapper = null;
        if (td != null) {
            thinWrapper = DatasetUtils.createThinDatasetWrapper(td, inputDescriptor.getSecondaryType(), options);
        }

        Dataset<MoleculeObject> dataset;
        if (thinWrapper != null) {
            dataset = thinWrapper.prepareInput(input);
        } else {
            dataset = input;
        }

        String endpoint = getHttpExecutionEndpoint();

        InputStream content = JsonHandler.getInstance().marshalStreamToJsonArray(dataset.getStream(), false);
//            String inputData = IOUtils.convertStreamToString(content);
//            LOG.info("Input: " + inputData);
//            content = new ByteArrayInputStream(inputData.getBytes());

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
        InputStream output = CamelUtils.doRequestUsingHeadersAndQueryParams(camelContext, "POST", endpoint, content, requestHeaders, responseHeaders, options);
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
        return results;
    }

}
