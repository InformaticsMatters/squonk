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

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.camel.CamelContext;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.util.CamelUtils;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.dataset.*;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;
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

        HttpServiceDescriptor httpServiceDescriptor = getHttpServiceDescriptor();

        updateStatus(MSG_PREPARING_INPUT);

        Dataset<MoleculeObject> inputDataset = fetchMappedInput("input", Dataset.class, varman);
        String endpoint = getHttpExecutionEndpoint();

        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        // NOTE: setting the Content-Encoding will cause camel to gzip the data, we don't need to do it
        requestHeaders.put("Content-Encoding", "gzip");
        if (jobId != null) {
            requestHeaders.put(StatsRecorder.HEADER_SQUONK_JOB_ID, jobId);
        }

        IODescriptor inputDescriptor = getSingleInputDescriptor();
        ThinDescriptor td = getThinDescriptor(inputDescriptor);
        if (td == null) {
            throw new IllegalStateException("No ThinDescriptor was provided of could be inferred");
        }
        ThinDatasetWrapper thinWrapper = DatasetUtils.createThinDatasetWrapper(td, inputDescriptor.getSecondaryType(), options);
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
        statusMessage = generateStatusMessage(inputDataset.getSize(), resultDataset.getSize(), -1);
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(resultDataset.getMetadata()));
    }

}
