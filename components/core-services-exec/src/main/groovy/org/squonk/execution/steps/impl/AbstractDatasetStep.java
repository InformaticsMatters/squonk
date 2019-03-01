/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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
import org.squonk.execution.steps.AbstractThinStep;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Base executor for a step that has zero or one Datasets as input and one Dataset as output,
 *
 * @param <P>
 * @param <Q>
 */
public abstract class AbstractDatasetStep<P extends BasicObject, Q extends BasicObject>
        extends AbstractThinStep {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetStep.class.getName());

    /**
     * Specifies whether an input dataset is expected, which is typically the case.
     * Set to false if there is output but no input.
     */
    protected boolean inputRequired = true;

    /**
     * Execute with the given inputs
     *
     * @param inputs
     * @return
     * @throws Exception
     */
    @Override
    protected Map<String, Object> doExecute(Map<String, Object> inputs, CamelContext camelContext) throws Exception {
        if (inputRequired && inputs.size() == 0) {
            throw new IllegalArgumentException("Single dataset expected - found none");
        } else if (inputs.size() > 1) {
            throw new IllegalArgumentException("Single dataset expected - found " + inputs.size());
        } else if (!inputRequired && inputs.size() > 0) {
            throw new IllegalArgumentException("Input found but was not expected");
        }
        Dataset dataset = null;
        if (inputs.size() == 1) {
            Map.Entry<String, Object> entry = inputs.entrySet().iterator().next();
            String key = entry.getKey(); // value ignored, but presumed to be "input"
            Object value = entry.getValue();
            if (value == null) {
                throw new NullPointerException("No value present for input " + key);
            }
            if (value instanceof Dataset) {
                dataset = (Dataset) value;
            } else {
                throw new IllegalStateException("Input was not a dataset");
            }
        }
        Dataset<Q> results = doExecuteWithDataset(dataset, camelContext);
        return Collections.singletonMap("output", results);
    }

    /**
     * Override this method to implement the required functionality
     *
     * @param input Singe input dataset (can be null if the inputRequired property is set to false.
     * @param camelContext
     * @return
     * @throws Exception
     */
    protected abstract Dataset<Q> doExecuteWithDataset(Dataset<P> input, CamelContext camelContext) throws Exception;

    protected Dataset handleHttpPost(CamelContext camelContext, String endpoint, InputStream data, Map<String, Object> requestHeaders)  throws Exception{

        // send for execution
        updateStatus("Posting request ...");
        Map<String, Object> responseHeaders = new HashMap<>();
        InputStream resultsStream = CamelUtils.doRequestUsingHeadersAndQueryParams(camelContext, "POST", endpoint, data, requestHeaders, responseHeaders, options);
        updateStatus("Handling results ...");

//        // start debug output
//        String data = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(resultsStream), 1000);
//        LOG.info("Results: |" + data + "|");
//        resultsStream = new ByteArrayInputStream(data.getBytes());
//        // end debug output

        String responseMetadataJson = (String)responseHeaders.get(CamelCommonConstants.HEADER_METADATA);
        DatasetMetadata<? extends BasicObject> responseMetadata;
        if (responseMetadataJson == null || "null".equals(responseMetadataJson)) {
            responseMetadata = new DatasetMetadata<>(MoleculeObject.class);
            LOG.info("No Metadata returned from service. Assuming MoleculeObjects");
        } else {
            LOG.fine("Using response metadata: " + responseMetadataJson);
            responseMetadata = JsonHandler.getInstance().objectFromJson(responseMetadataJson, DatasetMetadata.class);
            int size = responseMetadata.getSize();
            if (size > 0) {
                updateStatus(size + " processed");
            } else {
                updateStatus("Processing complete");
            }
        }


        Dataset<? extends BasicObject> results = new Dataset(resultsStream, responseMetadata);
        addResultsCounter(results);

        return results;
    }
}
